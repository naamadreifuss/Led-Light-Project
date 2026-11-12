#include "LedController.h"

void LedController::begin(Adafruit_NeoMatrix &matrix)
{
    matrixPtr = &matrix;

    matrixPtr->begin();
    matrixPtr->setBrightness(20);
    matrixPtr->fillScreen(0);
    matrixPtr->show();
}

//color wheel
void LedController::setColor(uint8_t r_, uint8_t g_, uint8_t b_)
{
    r = r_;
    g = g_;
    b = b_;
}

void LedController::render()
{
    if (!matrixPtr)
        return;

    uint16_t color = matrixPtr->Color(r, g, b);

    matrixPtr->fillScreen(color);

    matrixPtr->show();
}

void LedController::turnOff()
{
    if (!matrixPtr)
        return;

    matrixPtr->fillScreen(0);
    matrixPtr->show();
}


void LedController::setText(const String& text)
{
    Text = text;

    if (!matrixPtr)
        return;

    textX = matrixPtr->width();
}


void LedController::setScrollSpeed(float speed)
{
    if (speed <= 0)
        return;

    scrollSpeed = speed;
}

//displaying scrolled text, for text & karaoke mode
void LedController::renderText()
{
    if (!matrixPtr)
        return;


    unsigned long scrollInterval =
        1000.0 / scrollSpeed;


    if (millis() - lastScroll < scrollInterval)
        return;


    lastScroll = millis();


    matrixPtr->fillScreen(0);


    matrixPtr->setTextWrap(false);
    matrixPtr->setTextSize(1);


    matrixPtr->setTextColor(
        matrixPtr->Color(r, g, b)
    );


    matrixPtr->setCursor(textX, 4);


    matrixPtr->print(Text);


    matrixPtr->show();


    textX--;


    int16_t textWidth =
        Text.length() * 6;


    if (textX < -textWidth)
    {
        textX = matrixPtr->width();
    }
}

//image mode
void LedController::showImage(const uint32_t* pixels)
{
    if (!matrixPtr)
        return;

    for (int y = 0; y < 16; y++)
    {
        for (int x = 0; x < 32; x++)
        {
            int index = y * 32 + x;

            uint32_t rgb = pixels[index];

            uint8_t r = (rgb >> 16) & 0xFF;
            uint8_t g = (rgb >> 8) & 0xFF;
            uint8_t b = rgb & 0xFF;

            matrixPtr->drawPixel(
                x,
                y,
                matrixPtr->Color(r, g, b)
            );
        }
    }

    matrixPtr->show();
}

//heart rate mode
void LedController::showHeartRate(int bpm)
{
    if (!matrixPtr)
        return;

if (bpm < 40 || bpm > 180)
{
    matrixPtr->fillScreen(0);
    matrixPtr->setTextSize(1);
    matrixPtr->setTextColor(matrixPtr->Color(255, 0, 0));
    matrixPtr->setCursor(4, 4);
    matrixPtr->print("No BPM");
    matrixPtr->show();
    return;
}

    // Small heart: 12x6 pixels.
    static const uint16_t smallHeart[6] = {
        0x660,
        0xFF0,
        0xFF0,
        0x7E0,
        0x3C0,
        0x180
    };

    // Large heart: 16x8 pixels.
    static const uint16_t largeHeart[8] = {
        0x6660,
        0xFFF0,
        0xFFF0,
        0x7FE0,
        0x3FC0,
        0x1F80,
        0x0F00,
        0x0600
    };

    // Calculate the current position within one heartbeat cycle.
    const unsigned long period = 60000UL / bpm;
    const unsigned long phase = millis() % period;

    // Default state: a small, dim red heart.
    bool useLargeHeart = false;
    uint8_t intensity = 45;

    // First, stronger heartbeat.
    if (phase < 120)
    {
        useLargeHeart = true;
        intensity = 255 - (210 * phase / 120);
    }
    // Second, weaker heartbeat.
    else if (phase >= 160 && phase < 240)
    {
        useLargeHeart = true;
        intensity = 150 - (105 * (phase - 160) / 80);
    }

    matrixPtr->fillScreen(0);

    // Red heart: red intensity changes during the heartbeat.
    const uint16_t heartColor = matrixPtr->Color(intensity, 0, 0);

    // Select the bitmap and center it horizontally.
    const uint16_t* heartBitmap;
    int heartWidth;
    int heartHeight;

    if (useLargeHeart)
    {
        heartBitmap = largeHeart;
        heartWidth = 16;
        heartHeight = 8;
    }
    else
    {
        heartBitmap = smallHeart;
        heartWidth = 12;
        heartHeight = 6;
    }

    const int xOffset = (matrixPtr->width() - heartWidth) / 2;
    const int yOffset = (9 - heartHeight) / 2;

    // Draw the heart one pixel at a time.
    for (int y = 0; y < heartHeight; y++)
    {
        for (int x = 0; x < heartWidth; x++)
        {
            if (heartBitmap[y] & (1U << (heartWidth - 1 - x)))
            {
                matrixPtr->drawPixel(
                    x + xOffset,
                    y + yOffset,
                    heartColor
                );
            }
        }
    }

    // Display the BPM in white at the bottom of the matrix.
    matrixPtr->setTextSize(1);
    matrixPtr->setTextColor(matrixPtr->Color(255, 255, 255));
    matrixPtr->setCursor(1, 9);
    matrixPtr->print(bpm);
    matrixPtr->print("bpm");

    matrixPtr->show();
}

