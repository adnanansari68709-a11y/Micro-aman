package com.example.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

// MacroDroid Core Paradigm: Triggers, Actions, Constraints
data class Macro(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val category: String = "General",
    val enabled: Boolean = true,
    val triggers: List<MacroTrigger> = emptyList(),
    val actions: List<MacroAction> = emptyList(),
    val constraints: List<MacroConstraint> = emptyList(),
    val lastTriggeredAt: Long = 0L,
    val triggerCount: Int = 0,
    val colorTag: String = "#00E5FF"
) {
    fun toJson(): String {
        val root = JSONObject()
        root.put("id", id)
        root.put("name", name)
        root.put("description", description)
        root.put("category", category)
        root.put("enabled", enabled)
        root.put("lastTriggeredAt", lastTriggeredAt)
        root.put("triggerCount", triggerCount)
        root.put("colorTag", colorTag)

        val triggersArray = JSONArray()
        triggers.forEach { triggersArray.put(it.toJSONObject()) }
        root.put("triggers", triggersArray)

        val actionsArray = JSONArray()
        actions.forEach { actionsArray.put(it.toJSONObject()) }
        root.put("actions", actionsArray)

        val constraintsArray = JSONArray()
        constraints.forEach { constraintsArray.put(it.toJSONObject()) }
        root.put("constraints", constraintsArray)

        return root.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): Macro {
            val root = JSONObject(jsonStr)
            val triggersList = mutableListOf<MacroTrigger>()
            val triggersArray = root.optJSONArray("triggers") ?: JSONArray()
            for (i in 0 until triggersArray.length()) {
                val obj = triggersArray.getJSONObject(i)
                MacroTrigger.fromJSONObject(obj)?.let { triggersList.add(it) }
            }

            val actionsList = mutableListOf<MacroAction>()
            val actionsArray = root.optJSONArray("actions") ?: JSONArray()
            for (i in 0 until actionsArray.length()) {
                val obj = actionsArray.getJSONObject(i)
                MacroAction.fromJSONObject(obj)?.let { actionsList.add(it) }
            }

            val constraintsList = mutableListOf<MacroConstraint>()
            val constraintsArray = root.optJSONArray("constraints") ?: JSONArray()
            for (i in 0 until constraintsArray.length()) {
                val obj = constraintsArray.getJSONObject(i)
                MacroConstraint.fromJSONObject(obj)?.let { constraintsList.add(it) }
            }

            return Macro(
                id = root.optString("id", UUID.randomUUID().toString()),
                name = root.optString("name", "Unnamed Macro"),
                description = root.optString("description", ""),
                category = root.optString("category", "General"),
                enabled = root.optBoolean("enabled", true),
                triggers = triggersList,
                actions = actionsList,
                constraints = constraintsList,
                lastTriggeredAt = root.optLong("lastTriggeredAt", 0L),
                triggerCount = root.optInt("triggerCount", 0),
                colorTag = root.optString("colorTag", "#00E5FF")
            )
        }
    }
}

// ----------------- TRIGGERS (RED) -----------------
enum class TriggerType(val title: String, val category: String) {
    BATTERY_LEVEL("Battery Level", "Battery & Power"),
    POWER_CONNECTED("Charger Connected", "Battery & Power"),
    POWER_DISCONNECTED("Charger Disconnected", "Battery & Power"),
    SCREEN_ON("Screen Turned On", "Device Screen"),
    SCREEN_OFF("Screen Turned Off", "Device Screen"),
    DEVICE_UNLOCKED("Device Unlocked", "Device Screen"),
    SHAKE_DEVICE("Shake Device", "Sensors & Motion"),
    FLIP_DEVICE("Flip Phone (Face Down)", "Sensors & Motion"),
    TIME_OF_DAY("Regular Time", "Date & Time"),
    INTERVAL_TIMER("Interval Timer", "Date & Time"),
    WIFI_CONNECTED("Wi-Fi Connected", "Connectivity"),
    WIFI_DISCONNECTED("Wi-Fi Disconnected", "Connectivity"),
    HEADPHONES_PLUGGED("Headphones Plugged In", "Device Hardware"),
    HEADPHONES_UNPLUGGED("Headphones Unplugged", "Device Hardware"),
    BOOT_COMPLETED("Device Boot / Startup", "System Events"),
    WHATSAPP_MESSAGE_RECEIVED("WhatsApp Message Received", "Social & Messaging"),
    SMS_RECEIVED("SMS Message Received", "Social & Messaging")
}

data class MacroTrigger(
    val id: String = UUID.randomUUID().toString(),
    val type: TriggerType,
    val summary: String,
    val params: Map<String, String> = emptyMap()
) {
    fun toJSONObject(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("type", type.name)
        obj.put("summary", summary)
        val p = JSONObject()
        params.forEach { (k, v) -> p.put(k, v) }
        obj.put("params", p)
        return obj
    }

    companion object {
        fun fromJSONObject(obj: JSONObject): MacroTrigger? {
            val typeName = obj.optString("type")
            val type = try {
                TriggerType.valueOf(typeName)
            } catch (e: Exception) {
                return null
            }
            val map = mutableMapOf<String, String>()
            val p = obj.optJSONObject("params")
            p?.keys()?.forEach { key ->
                map[key] = p.getString(key)
            }
            return MacroTrigger(
                id = obj.optString("id", UUID.randomUUID().toString()),
                type = type,
                summary = obj.optString("summary", type.title),
                params = map
            )
        }
    }
}

// ----------------- ACTIONS (BLUE) -----------------
enum class ActionType(val title: String, val category: String) {
    SPEAK_TEXT("Speak Text (TTS)", "Speech & Audio"),
    SHOW_NOTIFICATION("Show Notification", "Alerts & UI"),
    PLAY_SOUND("Play Alert Sound", "Speech & Audio"),
    VIBRATE("Vibrate Device", "Device Settings"),
    TOGGLE_FLASHLIGHT("Toggle Torch / Flashlight", "Device Settings"),
    SET_VOLUME("Set Media / Ring Volume", "Device Settings"),
    LAUNCH_APP("Launch Application", "Apps & Links"),
    OPEN_URL("Open Website URL", "Apps & Links"),
    COPY_TO_CLIPBOARD("Copy to Clipboard", "Clipboard & Text"),
    SHOW_TOAST("Show Quick Toast", "Alerts & UI"),
    WAIT_DELAY("Wait / Delay", "Flow Control"),
    LOG_MESSAGE("Write to System Log", "Flow Control"),
    SET_RINGER_MODE("Set Ringer Mode (Silent/Vibrate/Normal)", "Device Settings"),
    WHATSAPP_AUTO_REPLY("WhatsApp Auto Reply", "Social & Messaging"),
    SEND_SMS("Send SMS / Auto-Reply", "Social & Messaging")
}

data class MacroAction(
    val id: String = UUID.randomUUID().toString(),
    val type: ActionType,
    val summary: String,
    val params: Map<String, String> = emptyMap()
) {
    fun toJSONObject(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("type", type.name)
        obj.put("summary", summary)
        val p = JSONObject()
        params.forEach { (k, v) -> p.put(k, v) }
        obj.put("params", p)
        return obj
    }

    companion object {
        fun fromJSONObject(obj: JSONObject): MacroAction? {
            val typeName = obj.optString("type")
            val type = try {
                ActionType.valueOf(typeName)
            } catch (e: Exception) {
                return null
            }
            val map = mutableMapOf<String, String>()
            val p = obj.optJSONObject("params")
            p?.keys()?.forEach { key ->
                map[key] = p.getString(key)
            }
            return MacroAction(
                id = obj.optString("id", UUID.randomUUID().toString()),
                type = type,
                summary = obj.optString("summary", type.title),
                params = map
            )
        }
    }
}

// ----------------- CONSTRAINTS (GREEN) -----------------
enum class ConstraintType(val title: String, val category: String) {
    BATTERY_LEVEL_RANGE("Battery Level %", "Battery"),
    POWER_STATE("Power State (Charging/Unplugged)", "Battery"),
    SCREEN_STATE("Screen State (On/Off)", "Device"),
    TIME_WINDOW("Time Window (Between X and Y)", "Date & Time"),
    WIFI_STATE("Wi-Fi Connected", "Connectivity")
}

data class MacroConstraint(
    val id: String = UUID.randomUUID().toString(),
    val type: ConstraintType,
    val summary: String,
    val params: Map<String, String> = emptyMap()
) {
    fun toJSONObject(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("type", type.name)
        obj.put("summary", summary)
        val p = JSONObject()
        params.forEach { (k, v) -> p.put(k, v) }
        obj.put("params", p)
        return obj
    }

    companion object {
        fun fromJSONObject(obj: JSONObject): MacroConstraint? {
            val typeName = obj.optString("type")
            val type = try {
                ConstraintType.valueOf(typeName)
            } catch (e: Exception) {
                return null
            }
            val map = mutableMapOf<String, String>()
            val p = obj.optJSONObject("params")
            p?.keys()?.forEach { key ->
                map[key] = p.getString(key)
            }
            return MacroConstraint(
                id = obj.optString("id", UUID.randomUUID().toString()),
                type = type,
                summary = obj.optString("summary", type.title),
                params = map
            )
        }
    }
}
