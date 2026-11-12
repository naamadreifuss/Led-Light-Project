#include "IconController.h"
#include "FirebaseManager.h"

// Initialize servos and laser
void IconController::begin(
    int servoXPin,
    int servoYPin,
    int laserPin
)
{
    this->servoXPin = servoXPin;
    this->servoYPin = servoYPin;
    this->laserPin = laserPin;

    // Servo initialization
    ESP32PWM::allocateTimer(0);
    ESP32PWM::allocateTimer(1);

    servoX.setPeriodHertz(50); //esp32 works with 50Hz, pulse every 20ms
    servoY.setPeriodHertz(50);

    servoX.attach(servoXPin, 500, 2400); //angle 0=500us, 180 =2400us
    servoY.attach(servoYPin, 500, 2400); //for example, angke of 90 is 1500us

    // Laser initialization
    pinMode(laserPin, OUTPUT);
    laserOff();

    // Move to center position
    servoX.writeMicroseconds(1500);
    servoY.writeMicroseconds(1500);

    delay(500);
}

// Stop laser and move to safe position
void IconController::turnOff()
{
    laserOff();
    // Return servos to center
    servoX.writeMicroseconds(1500);
    servoY.writeMicroseconds(1500);
}

// Turn laser on
void IconController::laserOn()
{
    digitalWrite(laserPin, HIGH);
}

// Turn laser off
void IconController::laserOff()
{
    digitalWrite(laserPin, LOW);
}

// Move laser to position
void IconController::moveTo(int x, int y)
{
    servoX.writeMicroseconds(x);
    servoY.writeMicroseconds(y);
}

// ========================================================
// ICON DRAWING FUNCTIONS
// ========================================================

// 1. Draw Heart
void IconController::drawHeart()
{
    Serial.println("Drawing heart");
    int centerX = 1500;
    int centerY = 1500;
    float scale = 12.0; // Scale factor for heart curve equation

    // Move to starting position (t = 0)
    float firstRad = 0;
    int startX = centerX + scale * 16 * pow(sin(firstRad), 3);
    int startY = centerY - scale * (13 * cos(firstRad) - 5 * cos(2 * firstRad) - 2 * cos(3 * firstRad) - cos(4 * firstRad));
    moveTo(startX, startY);
    delay(50);

    laserOn();

    for (float angle = 0; angle <= 360; angle += 3)
    {
        float t = angle * PI / 180.0;
        // Heart cardioid parametric formulas
        int x = centerX + scale * 16 * pow(sin(t), 3);
        int y = centerY - scale * (13 * cos(t) - 5 * cos(2 * t) - 2 * cos(3 * t) - cos(4 * t));
        
        moveTo(x, y);
        delay(8);
    }

    laserOff();
    Serial.println("Heart finished");
}

// 2. Draw Star (5-Pointed Star)
void IconController::drawStar()
{
    Serial.println("Drawing star");
    int centerX = 1500;
    int centerY = 1500;
    int outerRadius = 220;
    int innerRadius = 90;

    int points[11][2];

    // Calculate 10 vertices of a 5-pointed star
    for (int i = 0; i < 10; i++) {
        float r = (i % 2 == 0) ? outerRadius : innerRadius;
        float angle = (i * 36 - 90) * PI / 180.0; // Start pointing upwards
        points[i][0] = centerX + r * cos(angle);
        points[i][1] = centerY + r * sin(angle);
    }
    // Close the shape back to first point
    points[10][0] = points[0][0];
    points[10][1] = points[0][1];

    moveTo(points[0][0], points[0][1]);
    delay(50);
    laserOn();

    for (int i = 0; i < 10; i++) {
        int x1 = points[i][0], y1 = points[i][1];
        int x2 = points[i+1][0], y2 = points[i+1][1];

        for (int step = 0; step <= 10; step++) {
            int currentX = x1 + (x2 - x1) * step / 10;
            int currentY = y1 + (y2 - y1) * step / 10;
            moveTo(currentX, currentY);
            delay(10);
        }
    }

    laserOff();
    Serial.println("Star finished");
}

// 3. Draw Flower (Rose Curve / Petals)
void IconController::drawFlower()
{
    Serial.println("Drawing flower");
    int centerX = 1500;
    int centerY = 1500;
    int maxRadius = 200;
    int k = 5; // Number of petals

    // Move to start position
    float firstRad = 0;
    float r = maxRadius * cos(k * firstRad);
    moveTo(centerX + r * cos(firstRad), centerY + r * sin(firstRad));
    delay(50);

    laserOn();

    for (float angle = 0; angle <= 360; angle += 2)
    {
        float rad = angle * PI / 180.0;
        float currentRadius = maxRadius * cos(k * rad);
        int x = centerX + currentRadius * cos(rad);
        int y = centerY + currentRadius * sin(rad);
        
        moveTo(x, y);
        delay(8);
    }

    laserOff();
    Serial.println("Flower finished");
}

// 4. Draw Circle
void IconController::drawCircle()
{
    Serial.println("Drawing circle");
    laserOn();
    int centerX = 1500;
    int centerY = 1500;
    int radius = 200;
    for(float angle = 0; angle < 360; angle += 5)
    {
        float rad = angle * PI / 180.0;
        int x = centerX + radius * cos(rad);
        int y = centerY + radius * sin(rad);
        moveTo(x, y);
        delay(10);
    }
    laserOff();
    Serial.println("Circle finished");
}

// 5. Draw Triangle
void IconController::drawTriangle()
{
    Serial.println("Drawing triangle");
    int centerX = 1500;
    int centerY = 1500;
    int size = 200;

    int points[4][2] = {
        {centerX, centerY - size},           // Top
        {centerX + size, centerY + size},     // Bottom Right
        {centerX - size, centerY + size},     // Bottom Left
        {centerX, centerY - size}            // Close loop
    };

    moveTo(points[0][0], points[0][1]);
    delay(50);
    laserOn();

    for (int i = 0; i < 3; i++) {
        int x1 = points[i][0], y1 = points[i][1];
        int x2 = points[i+1][0], y2 = points[i+1][1];

        for (int step = 0; step <= 20; step++) {
            int currentX = x1 + (x2 - x1) * step / 20;
            int currentY = y1 + (y2 - y1) * step / 20;
            moveTo(currentX, currentY);
            delay(10);
        }
    }

    laserOff();
    Serial.println("Triangle finished");
}

// 6. Draw Smiley Face (Single continuous stroke)
void IconController::drawSmiley()
{
    Serial.println("Drawing smiley");
    int centerX = 1500;
    int centerY = 1500;
    int radius = 220;

    // 1. Move to start of outer circle (Top center)
    moveTo(centerX, centerY - radius);
    delay(50);
    laserOn();

    // 2. Draw outer circle
    for (float angle = -90; angle <= 270; angle += 5)
    {
        float rad = angle * PI / 180.0;
        int x = centerX + radius * cos(rad);
        int y = centerY + radius * sin(rad);
        moveTo(x, y);
        delay(8);
    }

    // 3. Move inwards to Left Eye
    int leftEyeX = centerX - 70;
    int leftEyeY = centerY - 70;
    for (int step = 0; step <= 10; step++) {
        int x = centerX + (leftEyeX - centerX) * step / 10;
        int y = (centerY - radius) + (leftEyeY - (centerY - radius)) * step / 10;
        moveTo(x, y);
        delay(5);
    }

    // 4. Move to Right Eye
    int rightEyeX = centerX + 70;
    int rightEyeY = centerY - 70;
    for (int step = 0; step <= 10; step++) {
        int x = leftEyeX + (rightEyeX - leftEyeX) * step / 10;
        int y = leftEyeY + (rightEyeY - leftEyeY) * step / 10;
        moveTo(x, y);
        delay(5);
    }

    // 5. Move down to start of Smile (Right side)
    int smileRightX = centerX + 100;
    int smileRightY = centerY + 30;
    for (int step = 0; step <= 10; step++) {
        int x = rightEyeX + (smileRightX - rightEyeX) * step / 10;
        int y = rightEyeY + (smileRightY - rightEyeY) * step / 10;
        moveTo(x, y);
        delay(5);
    }

    // 6. Draw Arc Smile to Left side
    for (float angle = 20; angle <= 160; angle += 5)
    {
        float rad = angle * PI / 180.0;
        int x = centerX + 100 * cos(rad);
        int y = centerY + 30 + 70 * sin(rad);
        moveTo(x, y);
        delay(8);
    }

    laserOff();
    Serial.println("Smiley finished");
}

// 7. Draw Arrow
void IconController::drawArrow()
{
    Serial.println("Drawing arrow");
    int centerX = 1500;
    int centerY = 1500;

    int points[8][2] = {
        {centerX - 50, centerY + 200},  // Stem Bottom-Left
        {centerX + 50, centerY + 200},  // Stem Bottom-Right
        {centerX + 50, centerY - 50},   // Stem Top-Right
        {centerX + 150, centerY - 50},  // Arrow Head Right Tip
        {centerX, centerY - 200},       // Arrow Top Point
        {centerX - 150, centerY - 50},  // Arrow Head Left Tip
        {centerX - 50, centerY - 50},   // Stem Top-Left
        {centerX - 50, centerY + 200}   // Close loop back to start
    };

    moveTo(points[0][0], points[0][1]);
    delay(50);
    laserOn();

    for (int i = 0; i < 7; i++) {
        int x1 = points[i][0], y1 = points[i][1];
        int x2 = points[i+1][0], y2 = points[i+1][1];

        for (int step = 0; step <= 15; step++) {
            int currentX = x1 + (x2 - x1) * step / 15;
            int currentY = y1 + (y2 - y1) * step / 15;
            moveTo(currentX, currentY);
            delay(10);
        }
    }

    laserOff();
    Serial.println("Arrow finished");
}

// 8. Draw Square
void IconController::drawSquare()
{
    Serial.println("Drawing square");
    int centerX = 1500;
    int centerY = 1500;
    int size = 200; // Half-width / Half-height

    int points[5][2] = {
        {centerX - size, centerY - size},
        {centerX + size, centerY - size},
        {centerX + size, centerY + size},
        {centerX - size, centerY + size},
        {centerX - size, centerY - size} // Close loop
    };

    // Move to start position before turning on laser
    moveTo(points[0][0], points[0][1]);
    delay(50);
    laserOn();

    // Draw lines with interpolation between corners
    for (int i = 0; i < 4; i++) {
        int x1 = points[i][0], y1 = points[i][1];
        int x2 = points[i+1][0], y2 = points[i+1][1];

        for (int step = 0; step <= 20; step++) {
            int currentX = x1 + (x2 - x1) * step / 20;
            int currentY = y1 + (y2 - y1) * step / 20;
            moveTo(currentX, currentY);
            delay(10);
        }
    }

    laserOff();
    Serial.println("Square finished");
}

//customIcon
// ========================================================
// CUSTOM ICON DRAWING
// ========================================================

void IconController::drawCustomIcon(
    const std::vector<CustomIconPoint>& points
)
{
    Serial.println("Drawing custom icon");

    // Check that there are enough points to draw
    if (points.size() < 2)
    {
        Serial.println("Not enough points for custom icon");
        return;
    }

    // Convert canvas coordinates (0-100) to servo pulse values.
    // The range 1300-1700 matches the scale used by built-in icons.
    auto mapX = [](float x)
    {
        return constrain((int)(1300 + x * 4), 1300, 1700);
    };

    auto mapY = [](float y)
    {
        return constrain((int)(1300 + y * 4), 1300, 1700);
    };

    // Move to the first point while the laser is OFF
    moveTo(
        mapX(points[0].x),
        mapY(points[0].y)
    );

    delay(100);

    // Start drawing
    laserOn();

    // Draw the path in the exact order received from Firebase
    for (size_t i = 1; i < points.size(); i++)
    {
        int x1 = mapX(points[i - 1].x);
        int y1 = mapY(points[i - 1].y);

        int x2 = mapX(points[i].x);
        int y2 = mapY(points[i].y);

        // Calculate the number of intermediate movements
        int distanceX = abs(x2 - x1);
        int distanceY = abs(y2 - y1);

        int steps = max(distanceX, distanceY) / 5;

        if (steps < 1)
            steps = 1;

        // Interpolate between consecutive points for smoother movement
        for (int step = 1; step <= steps; step++)
        {
            int currentX = x1 + (x2 - x1) * step / steps;
            int currentY = y1 + (y2 - y1) * step / steps;

            moveTo(currentX, currentY);

            delay(5);
        }
    }

    // Finish drawing
    laserOff();

    Serial.println("Custom icon finished");
}