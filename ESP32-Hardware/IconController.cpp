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

    servoX.setPeriodHertz(50); // ESP32 works with 50Hz, pulse every 20ms
    servoY.setPeriodHertz(50);

    servoX.attach(servoXPin, 500, 2400); // angle 0 = 500us, 180 = 2400us
    servoY.attach(servoYPin, 500, 2400); // angle 90 = 1500us

    // Laser initialization
    pinMode(laserPin, OUTPUT);
    returnToCenter();

    delay(250);
}

// Stop laser and move to safe neutral center
void IconController::turnOff()
{
    returnToCenter();
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

// Move laser to position (with Y-axis binding protection)
void IconController::moveTo(int x, int y)
{
    servoX.writeMicroseconds(constrain(x, 600, 2300));
    servoY.writeMicroseconds(constrain(y, 950, 2200)); // 950 min protects Y servo from mechanical binding
}

// Turn off laser and return to neutral center to prevent gear backlash drift
void IconController::returnToCenter()
{
    laserOff();
    moveTo(BASE_X, BASE_Y);
    delay(250);
}

// Helper: Draw a smooth cubic Bezier curve segment synced to 50Hz (20ms) servo frames
void IconController::drawBezierSegment(
    float x0, float y0,
    float x1, float y1,
    float x2, float y2,
    float x3, float y3,
    int steps, int stepDelayMs
)
{
    for (int i = 1; i <= steps; i++) {
        float t = (float)i / (float)steps;
        float u = 1.0f - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;

        float x = uuu * x0 + 3.0f * uu * t * x1 + 3.0f * u * tt * x2 + ttt * x3;
        float y = uuu * y0 + 3.0f * uu * t * y1 + 3.0f * u * tt * y2 + ttt * y3;

        moveTo((int)round(x), (int)round(y));
        delay(stepDelayMs);
    }
}

// Helper: Draw a straight line segment synced to 50Hz (20ms) servo frames
void IconController::drawLineSegment(
    float x0, float y0,
    float x1, float y1,
    int steps, int stepDelayMs
)
{
    for (int i = 1; i <= steps; i++) {
        float x = x0 + (x1 - x0) * i / (float)steps;
        float y = y0 + (y1 - y0) * i / (float)steps;
        moveTo((int)round(x), (int)round(y));
        delay(stepDelayMs);
    }
}

// ========================================================
// ICON DRAWING FUNCTIONS
// ========================================================

// 1. Draw Heart
void IconController::drawHeart()
{
    Serial.println("Drawing heart");
    float cx = BASE_X;
    float cy = BASE_Y - 32; // 1268
    float r = 135.0f;

    float cleftX = cx,             cleftY = cy + r * 0.34f;
    float rightX = cx + r * 0.96f, rightY = cy + r * 0.30f;
    float tipX   = cx,             tipY   = cy - r * 0.98f;
    float leftX  = cx - r * 0.96f, leftY  = cy + r * 0.30f;

    moveTo((int)round(cleftX), (int)round(cleftY));
    delay(250);

    laserOn();

    // 1. Top-right rounded dome (cleft -> right shoulder)
    drawBezierSegment(
        cleftX, cleftY,
        cx + r * 0.14f, cy + r * 1.00f,
        cx + r * 0.96f, cy + r * 1.00f,
        rightX, rightY,
        11, 20
    );

    // 2. Right curved side (right shoulder -> bottom tip)
    drawBezierSegment(
        rightX, rightY,
        cx + r * 0.96f, cy - r * 0.22f,
        cx + r * 0.52f, cy - r * 0.64f,
        tipX, tipY,
        12, 20
    );

    delay(60);

    // 3. Left curved side (bottom tip -> left shoulder)
    drawBezierSegment(
        tipX, tipY,
        cx - r * 0.52f, cy - r * 0.64f,
        cx - r * 0.96f, cy - r * 0.22f,
        leftX, leftY,
        12, 20
    );

    // 4. Top-left rounded dome (left shoulder -> top cleft)
    drawBezierSegment(
        leftX, leftY,
        cx - r * 0.96f, cy + r * 1.00f,
        cx - r * 0.14f, cy + r * 1.00f,
        cleftX, cleftY,
        11, 20
    );

    delay(100);
    returnToCenter();
    Serial.println("Heart finished");
}

// 2. Draw Star (5-Pointed Star)
void IconController::drawStar()
{
    Serial.println("Drawing star");
    int centerX = BASE_X;
    int centerY = BASE_Y - 35; // 1265
    float outerRadius = 145.0f;
    float innerRadius = 56.0f;

    int points[11][2];

    for (int i = 0; i < 10; i++) {
        float r = (i % 2 == 0) ? outerRadius : innerRadius;
        float angle = (i * 36 + 90) * PI / 180.0f;
        float dx = r * cos(angle);
        float dy = r * sin(angle);

        if (dy > 0) {
            dy *= 0.78f;
        }

        points[i][0] = centerX + round(dx);
        points[i][1] = centerY + round(dy);
    }
    points[10][0] = points[0][0];
    points[10][1] = points[0][1];

    moveTo(points[0][0], points[0][1]);
    delay(250);
    laserOn();

    for (int i = 0; i < 10; i++) {
        int x1 = points[i][0], y1 = points[i][1];
        int x2 = points[i + 1][0], y2 = points[i + 1][1];

        for (int step = 1; step <= 6; step++) {
            int currentX = x1 + (x2 - x1) * step / 6;
            int currentY = y1 + (y2 - y1) * step / 6;
            moveTo(currentX, currentY);
            delay(20);
        }

        if ((i + 1) % 2 == 0) {
            delay(40);
        }
    }

    delay(80);
    returnToCenter();
    Serial.println("Star finished");
}

// 3. Draw Sun (Center Circle + 8 Rays)
void IconController::drawSun()
{
    Serial.println("Drawing sun");
    float cx = BASE_X;
    float cy = BASE_Y - 30; // 1270
    float rSun = 88.0f;     // Center circle radius
    float rIn = 110.0f;     // Air gap outside center circle
    float rOut = 158.0f;    // Outer tip of each ray
    float ySquash = 0.72f;  // Keeps center circle & ray ring round on wall

    // 1. Center sun circle (34 steps + 3 overlap @ 20ms)
    moveTo((int)round(cx), (int)round(cy - rSun * ySquash));
    delay(250);
    laserOn();

    int circleSteps = 34;
    for (int i = 1; i <= circleSteps + 3; i++) {
        float rad = (-90.0f + 360.0f * i / (float)circleSteps) * PI / 180.0f;
        float x = cx + rSun * cos(rad);
        float y = cy + rSun * sin(rad) * ySquash;
        moveTo((int)round(x), (int)round(y));
        delay(20);
    }
    laserOff();

    // 2. Eight separate straight rays (0, 45, 90, 135, 180, 225, 270, 315 deg)
    for (int k = 0; k < 8; k++) {
        float rad = (k * 45.0f) * PI / 180.0f;
        float x0 = cx + rIn * cos(rad);
        float y0 = cy + rIn * sin(rad) * ySquash;
        float x1 = cx + rOut * cos(rad);
        float y1 = cy + rOut * sin(rad) * ySquash;

        moveTo((int)round(x0), (int)round(y0));
        delay(180);

        laserOn();
        drawLineSegment(x0, y0, x1, y1, 3, 20);
        delay(30);
        laserOff();
    }

    returnToCenter();
    Serial.println("Sun finished");
}

// 4. Draw Sad Smiley
void IconController::drawSadSmiley()
{
    Serial.println("Drawing sad smiley");
    float cx = BASE_X;
    float cy = BASE_Y - 30; // 1270
    float r = 138.0f;
    float ySquash = 0.72f;

    // 1. Round face outline (40 steps + 2 overlap)
    moveTo((int)round(cx), (int)round(cy - r * ySquash));
    delay(250);
    laserOn();

    int faceSteps = 40;
    for (int i = 1; i <= faceSteps + 2; i++) {
        float rad = (-90.0f + 360.0f * i / (float)faceSteps) * PI / 180.0f;
        float x = cx + r * cos(rad);
        float y = cy + r * sin(rad) * ySquash;
        moveTo((int)round(x), (int)round(y));
        delay(20);
    }
    laserOff();

    // 2. Left Eye (stationary dot)
    int leftEyeX = (int)round(cx - 48);
    int leftEyeY = (int)round(cy + 28);
    moveTo(leftEyeX, leftEyeY);
    delay(260);
    laserOn();
    delay(140);
    laserOff();

    // 3. Right Eye (stationary dot)
    int rightEyeX = (int)round(cx + 48);
    int rightEyeY = (int)round(cy + 28);
    moveTo(rightEyeX, rightEyeY);
    delay(260);
    laserOn();
    delay(140);
    laserOff();

    // 4. Sad Frown Arc (195 deg -> -15 deg)
    float frownRx = 78.0f;
    float frownRy = 68.0f;
    float frownCenterY = cy - 58;

    float startRad = 195.0f * PI / 180.0f;
    moveTo((int)round(cx + frownRx * cos(startRad)),
           (int)round(frownCenterY + frownRy * sin(startRad)));
    delay(240);
    laserOn();

    int frownSteps = 16;
    for (int i = 1; i <= frownSteps; i++) {
        float deg = 195.0f - (210.0f * i / (float)frownSteps);
        float rad = deg * PI / 180.0f;
        int x = (int)round(cx + frownRx * cos(rad));
        int y = (int)round(frownCenterY + frownRy * sin(rad));
        moveTo(x, y);
        delay(20);
    }

    delay(60);
    returnToCenter();
    Serial.println("Sad smiley finished");
}

// 5. Draw Triangle
void IconController::drawTriangle()
{
    Serial.println("Drawing triangle");
    int centerX = BASE_X - 40; // 1460
    int centerY = BASE_Y - 50; // 1250

    int points[4][2] = {
        {centerX - 152, centerY - 72},  // 0: Bottom-Left
        {centerX + 152, centerY - 72},  // 1: Bottom-Right
        {centerX,       centerY + 128}, // 2: Top Peak
        {centerX - 152, centerY - 72}   // 3: Close at Bottom-Left
    };

    moveTo(points[0][0], points[0][1]);
    delay(250);
    laserOn();

    for (int i = 0; i < 3; i++) {
        int x1 = points[i][0], y1 = points[i][1];
        int x2 = points[i + 1][0], y2 = points[i + 1][1];

        for (int step = 1; step <= 16; step++) {
            int currentX = x1 + (x2 - x1) * step / 16;
            int currentY = y1 + (y2 - y1) * step / 16;
            moveTo(currentX, currentY);
            delay(20);
        }

        if (i == 1) {
            delay(80);
        } else {
            delay(40);
        }
    }

    delay(60);
    returnToCenter();
    Serial.println("Triangle finished");
}

// 6. Draw Smiley Face
void IconController::drawSmiley()
{
    Serial.println("Drawing smiley");
    float cx = BASE_X;
    float cy = BASE_Y - 30; // 1270
    float r = 138.0f;
    float ySquash = 0.72f;

    // 1. Round face outline (40 steps + 2 overlap)
    moveTo((int)round(cx), (int)round(cy - r * ySquash));
    delay(250);
    laserOn();

    int faceSteps = 40;
    for (int i = 1; i <= faceSteps + 2; i++) {
        float rad = (-90.0f + 360.0f * i / (float)faceSteps) * PI / 180.0f;
        float x = cx + r * cos(rad);
        float y = cy + r * sin(rad) * ySquash;
        moveTo((int)round(x), (int)round(y));
        delay(20);
    }
    laserOff();

    // 2. Left Eye (stationary dot)
    int leftEyeX = (int)round(cx - 48);
    int leftEyeY = (int)round(cy + 28);
    moveTo(leftEyeX, leftEyeY);
    delay(260);
    laserOn();
    delay(140);
    laserOff();

    // 3. Right Eye (stationary dot)
    int rightEyeX = (int)round(cx + 48);
    int rightEyeY = (int)round(cy + 28);
    moveTo(rightEyeX, rightEyeY);
    delay(260);
    laserOn();
    delay(140);
    laserOff();

    // 4. Deep Happy U-Smile Arc (185 deg -> 355 deg)
    float smileRx = 78.0f;
    float smileRy = 68.0f;
    float smileCenterY = cy - 4;

    float startRad = 185.0f * PI / 180.0f;
    moveTo((int)round(cx + smileRx * cos(startRad)),
           (int)round(smileCenterY + smileRy * sin(startRad)));
    delay(240);
    laserOn();

    int smileSteps = 14;
    for (int i = 1; i <= smileSteps; i++) {
        float deg = 185.0f + (170.0f * i / (float)smileSteps);
        float rad = deg * PI / 180.0f;
        int x = (int)round(cx + smileRx * cos(rad));
        int y = (int)round(smileCenterY + smileRy * sin(rad));
        moveTo(x, y);
        delay(20);
    }

    delay(60);
    returnToCenter();
    Serial.println("Smiley finished");
}

// 7. Draw Arrow
void IconController::drawArrow()
{
    Serial.println("Drawing arrow");
    int centerX = BASE_X;
    int centerY = BASE_Y - 35; // 1265

    int points[8][2] = {
        {centerX - 38,  centerY - 110}, // Stem Bottom-Left
        {centerX + 38,  centerY - 110}, // Stem Bottom-Right
        {centerX + 38,  centerY + 6},   // Stem Top-Right
        {centerX + 112, centerY + 6},   // Arrow Head Right Tip
        {centerX,       centerY + 110}, // Arrow Top Point
        {centerX - 112, centerY + 6},   // Arrow Head Left Tip
        {centerX - 38,  centerY + 6},   // Stem Top-Left
        {centerX - 38,  centerY - 110}  // Close loop back to start
    };
    int edgeSteps[7] = {5, 7, 4, 9, 9, 4, 7};

    moveTo(points[0][0], points[0][1]);
    delay(250);
    laserOn();

    for (int i = 0; i < 7; i++) {
        int x1 = points[i][0], y1 = points[i][1];
        int x2 = points[i + 1][0], y2 = points[i + 1][1];
        int steps = edgeSteps[i];

        for (int step = 1; step <= steps; step++) {
            int currentX = x1 + (x2 - x1) * step / steps;
            int currentY = y1 + (y2 - y1) * step / steps;
            moveTo(currentX, currentY);
            delay(20);
        }
        delay(40);
    }

    delay(60);
    returnToCenter();
    Serial.println("Arrow finished");
}

// 8. Draw Square
void IconController::drawSquare()
{
    Serial.println("Drawing square");
    int centerX = BASE_X;
    int centerY = BASE_Y - 35; // 1265
    int halfW = 115;
    int halfH = 82;

    int points[5][2] = {
        {centerX - halfW, centerY + halfH},
        {centerX + halfW, centerY + halfH},
        {centerX + halfW, centerY - halfH},
        {centerX - halfW, centerY - halfH},
        {centerX - halfW, centerY + halfH} // Close loop
    };
    int edgeSteps[4] = {14, 10, 14, 10};

    moveTo(points[0][0], points[0][1]);
    delay(250);
    laserOn();

    for (int i = 0; i < 4; i++) {
        int x1 = points[i][0], y1 = points[i][1];
        int x2 = points[i + 1][0], y2 = points[i + 1][1];
        int steps = edgeSteps[i];

        for (int step = 1; step <= steps; step++) {
            int currentX = x1 + (x2 - x1) * step / steps;
            int currentY = y1 + (y2 - y1) * step / steps;
            moveTo(currentX, currentY);
            delay(20);
        }
        delay(40);
    }

    delay(60);
    returnToCenter();
    Serial.println("Square finished");
}

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

    // Finish drawing and return to neutral center
    returnToCenter();

    Serial.println("Custom icon finished");
}