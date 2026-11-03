package com.Lia.assistant.action

import org.json.JSONArray
import org.json.JSONObject

object ToolDeclHelper {

    fun functionDecl(
        name: String,
        description: String,
        params: Map<String, String>,
        required: List<String> = emptyList()
    ): JSONObject {
        val decl = JSONObject()
        decl.put("name", name)
        decl.put("description", description)

        val parameters = JSONObject()
        parameters.put("type", "OBJECT")

        val properties = JSONObject()
        for ((paramName, paramDesc) in params) {
            val prop = JSONObject()
            prop.put("type", "STRING")
            prop.put("description", paramDesc)
            properties.put(paramName, prop)
        }
        parameters.put("properties", properties)

        if (required.isNotEmpty()) {
            val reqArray = JSONArray()
            for (req in required) {
                reqArray.put(req)
            }
            parameters.put("required", reqArray)
        }

        decl.put("parameters", parameters)
        return decl
    }
}
