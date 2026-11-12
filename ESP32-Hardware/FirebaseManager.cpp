
#include "FirebaseManager.h"
#include "secrets.h"


// =========================
// Initialize Firebase
// =========================

void FirebaseManager::begin()
{
    Serial.println("\n=== Firebase START ===");

    config.api_key = API_KEY;
    config.database_url = DATABASE_URL;

    auth.user.email = USER_EMAIL;
    auth.user.password = USER_PASSWORD;

    Firebase.reconnectNetwork(true);

    commandsStream.setBSSLBufferSize(8192, 2048);
    commandsStream.setResponseSize(4096);

    statusStream.setBSSLBufferSize(8192, 2048);
    statusStream.setResponseSize(4096);

    imageStream.setBSSLBufferSize(16384, 4096);
    imageStream.setResponseSize(16384);

    Firebase.begin(&config, &auth);

    config.timeout.serverResponse = 10000;

    Serial.println("Firebase initialized");


    // =========================
    // Commands stream
    // =========================

    if (!Firebase.RTDB.beginStream(
            &commandsStream,
            "/commands"))
    {
        Serial.println("Commands stream FAILED");
        Serial.println(commandsStream.errorReason());
    }
    else
    {
        Serial.println("Commands stream started");
    }


    // =========================
    // Active mode stream
    // =========================

    if (!Firebase.RTDB.beginStream(
            &statusStream,
            "/status/activeMode"))
    {
        Serial.println("Status stream FAILED");
        Serial.println(statusStream.errorReason());
    }
    else
    {
        Serial.println("Status stream started");
    }


    // =========================
    // Image stream
    // =========================

    if (!Firebase.RTDB.beginStream(
            &imageStream,
            "/commands/image/pixels"))
    {
        Serial.println("Image stream FAILED");
        Serial.println(imageStream.errorReason());
    }
    else
    {
        Serial.println("Image stream started");
    }

    connected = true;
}


// =========================
// Update Firebase streams
// =========================

void FirebaseManager::update()
{
    if (!connected || !Firebase.ready())
        return;


    // =========================
    // COMMANDS STREAM
    // =========================

    if (Firebase.RTDB.readStream(&commandsStream))
    {
        if (!commandsStream.httpConnected())
        {
            Firebase.RTDB.endStream(&commandsStream);

            Firebase.RTDB.beginStream(
                &commandsStream,
                "/commands");
        }

        if (commandsStream.streamAvailable())
        {
            String path = commandsStream.dataPath();
            String dataType = commandsStream.dataType();
            String data = "";

            // Read JSON objects using jsonString()
            if (dataType == "json")
            {
                data = commandsStream.jsonString();
            }
            else
            {
                data = commandsStream.stringData();
            }

            Serial.println("========== COMMAND STREAM ==========");

            Serial.print("Path: ");
            Serial.println(path);

            Serial.print("Data type: ");
            Serial.println(dataType);

            Serial.print("Data: ");
            Serial.println(data);

            Serial.println("====================================");

            handleCommandsStream(path, data);
        }
    }


    // =========================
    // STATUS STREAM
    // =========================

    if (Firebase.RTDB.readStream(&statusStream))
    {
        if (!statusStream.httpConnected())
        {
            Firebase.RTDB.endStream(&statusStream);

            Firebase.RTDB.beginStream(
                &statusStream,
                "/status/activeMode");
        }

        if (statusStream.streamAvailable())
        {
            String data = statusStream.stringData();
            data.replace("\"", "");

            if (state.activeMode != data)
            {
                state.activeMode = data;
                state.modeChanged = true;

                Serial.print("Mode changed: ");
                Serial.println(data);
            }
        }
    }


    // =========================
    // IMAGE STREAM
    // =========================

    if (Firebase.RTDB.readStream(&imageStream))
    {
        if (!imageStream.httpConnected())
        {
            Firebase.RTDB.endStream(&imageStream);

            Firebase.RTDB.beginStream(
                &imageStream,
                "/commands/image/pixels");
        }

        if (imageStream.streamAvailable())
        {
            FirebaseJsonArray arr;

            if (arr.setJsonArrayData(imageStream.stringData()))
            {
                FirebaseJsonData value;

                size_t count = arr.size();

                if (count > NUM_PIXELS)
                    count = NUM_PIXELS;

                for (size_t i = 0; i < count; i++)
                {
                    arr.get(value, i);

                    state.pixels[i] =
                        (uint32_t)value.to<int>();
                }

                state.commandChanged = true;

                Serial.print("Image loaded: ");
                Serial.print(count);
                Serial.println(" pixels");
            }
        }
    }
}


// =========================
// Handle Commands Stream
// =========================

void FirebaseManager::handleCommandsStream(
    String path,
    String data)
{

    // =========================
    // INITIAL JSON OBJECT
    // =========================

    if (path == "/")
    {
        FirebaseJson json;
        FirebaseJsonData value;

        if (!json.setJsonData(data))
        {
            Serial.println("Initial commands JSON parsing failed");
            return;
        }

        // Read initial BPM
        if (json.get(value, "bpm"))
        {
            state.bpm = value.intValue;
            state.commandChanged = true;

            Serial.print("Initial BPM loaded: ");
            Serial.println(state.bpm);
        }

        // Read initial music mode
        if (json.get(value, "musicMode"))
        {
            state.musicMode = value.stringValue;
        }

        // Read initial text
        if (json.get(value, "text"))
        {
            state.text = value.stringValue;
        }

        // Read initial icon
        if (json.get(value, "icon"))
        {
            state.icon = value.stringValue;
        }

        // Read initial colors
        FirebaseJsonData colorValue;

        if (json.get(colorValue, "colors/r"))
            state.r = colorValue.intValue;

        if (json.get(colorValue, "colors/g"))
            state.g = colorValue.intValue;

        if (json.get(colorValue, "colors/b"))
            state.b = colorValue.intValue;

        // Read custom icon if it exists
        if (json.get(value, "customIcon"))
        {
            String customIconData = value.to<String>();

            handleCommandsStream("/customIcon", customIconData);
        }

        state.commandChanged = true;

        Serial.println("Initial commands loaded");

        return;
    }


    // =========================
    // HEART RATE
    // =========================

    if (path == "/bpm")
    {
        int receivedBpm = data.toInt();

        state.bpm = receivedBpm;
        state.commandChanged = true;

        Serial.print("BPM updated from Firebase: ");
        Serial.println(state.bpm);

        return;
    }


    // =========================
    // MUSIC MODE
    // =========================

    if (path == "/musicMode")
    {
        data.replace("\"", "");
        state.musicMode = data;

        state.commandChanged = true;

        Serial.print("Music mode updated: ");
        Serial.println(state.musicMode);

        return;
    }


    // =========================
    // COLORS
    // =========================

    if (path == "/colors")
    {
        FirebaseJson json;
        FirebaseJsonData value;

        if (json.setJsonData(data))
        {
            if (json.get(value, "r"))
                state.r = value.intValue;

            if (json.get(value, "g"))
                state.g = value.intValue;

            if (json.get(value, "b"))
                state.b = value.intValue;
        }

        state.commandChanged = true;

        Serial.println("Color updated");

        return;
    }


    // =========================
    // TEXT
    // =========================

    if (path == "/text")
    {
        data.replace("\"", "");
        state.text = data;

        state.commandChanged = true;

        Serial.println("Text updated");

        return;
    }


    // =========================
    // LYRIC LINE
    // =========================

    if (path == "/currentLine")
    {
        FirebaseJson json;
        FirebaseJsonData value;

        if (json.setJsonData(data))
        {
            if (json.get(value, "currentLine"))
            {
                state.currentLine.text =
                    value.stringValue;
            }

            if (json.get(value, "scrollSpeed"))
            {
                state.currentLine.scrollSpeed =
                    value.floatValue;
            }
        }

        state.commandChanged = true;

        Serial.print("Line updated: ");
        Serial.println(state.currentLine.text);

        Serial.print("Scroll speed: ");
        Serial.println(state.currentLine.scrollSpeed);

        return;
    }


    // =========================
    // ICON
    // =========================

    if (path == "/icon")
    {
        data.replace("\"", "");
        state.icon = data;

        state.commandChanged = true;

        Serial.println("Icon updated");

        return;
    }


    // =========================
    // CUSTOM ICON
    // =========================

    if (path == "/customIcon")
    {
        FirebaseJson customIconJson;
        FirebaseJsonData value;

        if (!customIconJson.setJsonData(data))
        {
            Serial.println("Custom icon JSON parsing failed");
            return;
        }

        // Read point count
        if (!customIconJson.get(value, "pointCount"))
        {
            Serial.println("pointCount missing");
            return;
        }

        int pointCount = value.intValue;

        if (pointCount <= 0 || pointCount > 1000)
        {
            Serial.println("Invalid point count");
            return;
        }

        // Read points array
        if (!customIconJson.get(value, "points"))
        {
            Serial.println("Points array missing");
            return;
        }

        FirebaseJsonArray pointsArray;

        if (!pointsArray.setJsonArrayData(value.to<String>()))
        {
            Serial.println("Points array parsing failed");
            return;
        }

        if (pointsArray.size() != pointCount)
        {
            Serial.println("Point count mismatch");
            return;
        }

        std::vector<CustomIconPoint> receivedPoints;

        receivedPoints.reserve(pointCount);

        // Read points one by one
        for (int i = 0; i < pointCount; i++)
        {
            FirebaseJsonData pointData;

            pointsArray.get(pointData, i);

            FirebaseJson pointJson;

            if (!pointJson.setJsonData(pointData.to<String>()))
            {
                Serial.print("Failed to parse point ");
                Serial.println(i);
                return;
            }

            CustomIconPoint point;

            if (!pointJson.get(value, "x"))
            {
                Serial.print("Missing X at point ");
                Serial.println(i);
                return;
            }

            point.x = value.floatValue;

            if (!pointJson.get(value, "y"))
            {
                Serial.print("Missing Y at point ");
                Serial.println(i);
                return;
            }

            point.y = value.floatValue;

            receivedPoints.push_back(point);
        }

        // Save only after all points were read successfully
        state.customIconPoints = receivedPoints;

        state.customIconChanged = true;
        state.commandChanged = true;

        Serial.print("Custom icon received: ");
        Serial.print(pointCount);
        Serial.println(" points");

        for (size_t i = 0; i < state.customIconPoints.size(); i++)
        {
            Serial.print("Point ");
            Serial.print(i);

            Serial.print(" | X: ");
            Serial.print(state.customIconPoints[i].x);

            Serial.print(" | Y: ");
            Serial.println(state.customIconPoints[i].y);
        }

        return;
    }
}


// =========================
// Get Current State
// =========================

const ServerCommands& FirebaseManager::getState() const
{
    return state;
}


// =========================
// Connection Status
// =========================

bool FirebaseManager::isConnected()
{
    return connected && Firebase.ready();
}


// =========================
// Clear Flags
// =========================

void FirebaseManager::clearFlags()
{
    state.modeChanged = false;
    state.commandChanged = false;
    state.customIconChanged = false;
}
