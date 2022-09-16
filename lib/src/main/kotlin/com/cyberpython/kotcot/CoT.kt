package com.cyberpython.kotcot

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.time.ZonedDateTime

@JacksonXmlRootElement(localName = "event")
data class Event(
        @JsonProperty(required = true) val point : Point,
        val detail: Any?,
        @JacksonXmlProperty(isAttribute = true) @JsonProperty(required = true) val version: Int = 2,
        @JacksonXmlProperty(isAttribute = true) @JsonProperty(required = true) val type: String,
        @JacksonXmlProperty(isAttribute = true) val access: String?,
        @JacksonXmlProperty(isAttribute = true) val qos: String?, // TODO: replace with QoS data class
        @JacksonXmlProperty(isAttribute = true) @JsonProperty(required = true) val uid: String,
        @JacksonXmlProperty(isAttribute = true) @JsonProperty(required = true) val time: ZonedDateTime,
        @JacksonXmlProperty(isAttribute = true) @JsonProperty(required = true) val start: ZonedDateTime,
        @JacksonXmlProperty(isAttribute = true) @JsonProperty(required = true) val stale: ZonedDateTime,
        @JacksonXmlProperty(isAttribute = true) @JsonProperty(required = true) val how: String
){
    init {
        require(version >= 2) { "Version must be greater than 2." }
        require(type.matches(Regex("^\\w+(-\\w+)*(;[^;]*)?$"))) { "Type must comprise of one or more word characters followed by '-' and one or more word characters. Additional components may follow, delimeted by ';'." }
        require(qos?.matches(Regex("^\\d-[rfi]-[gcd]$")) ?: true ) { "QoS (if defined) must be of the form 'priotiy-overtaking-assurance'." }
        require(how.matches(Regex("^\\w(-\\w+)*$"))) { "How must comprise of one word character followed by '-' and one or more word characters." }
    }

    fun getDetail(detailProperty: String): String {
        var currentProp = detail as Map<String, Any>
        val props = detailProperty.split(".")
        val lastProp =  props.last()
        props.dropLast(1).forEach{
            prop -> run {
                if(currentProp.containsKey(prop)) {
                    currentProp = currentProp[prop] as Map<String, Any>
                } else {
                    throw IllegalArgumentException("Invalid property: $detailProperty")
                }
            }
        }
        return currentProp[lastProp] as String
    }
}

data class Point(
    @JacksonXmlProperty(isAttribute = true) @JsonProperty(required = true) val lat: Double,
    @JacksonXmlProperty(isAttribute = true) @JsonProperty(required = true) val lon: Double,
    @JacksonXmlProperty(isAttribute = true) @JsonProperty(required = true) val hae: Double,
    @JacksonXmlProperty(isAttribute = true) @JsonProperty(required = true) val ce: Double,
    @JacksonXmlProperty(isAttribute = true) @JsonProperty(required = true) val le: Double,
){
    init {
        require(lat >= -90.0 && lat <= 90.0) { "Latitude must be between -90.0 and 90.0." }
        require(lon >= -180.0 && lon <= 180.0) { "Longitude must be between -180.0 and 180.0." }
    }
}

class CoT {

    companion object {
        val mapper = XmlMapper().registerModule(JavaTimeModule()).registerKotlinModule()
        init{
            mapper.enable(SerializationFeature.INDENT_OUTPUT)
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            mapper.setSerializationInclusion(Include.NON_NULL);
        }
    }

    fun write(e : Event) : String{
        return mapper.writeValueAsString(e)
    }

    fun write(f: File, e : Event){
        return mapper.writeValue(f, e)
    }

    fun parse(xml : String) : Event{
        return mapper.readValue(xml)
    }

    fun parse(xml : File) : Event{
        return mapper.readValue(xml)
    }
    
}

class CoTType2SIDC {

    fun to2525B(cotType: String) : String {
        require(cotType.matches(Regex("^a-[puafnshjkox]-[PAGSUFXZ](-\\w+)*$"))){"CoT to 2525B can only be applied to well-formed Atom type CoT Events."}
        val m2525bChars = cotType.substring(4).replace(Regex("[^A-Z0-9]+"), "")
        val m2525bBattleDim = m2525bChars.substring(0,1)
        val m2525bAffiliation = when (val cotAffiliation = cotType.substring(2, 3)) {
            "o", "x" -> "U"
            else -> cotAffiliation.uppercase()
        }
        val m2525bFuncId =  if (m2525bChars.length > 1) m2525bChars.substring(1).padEnd(6, '-').substring(0,6) else "------"
        return "S${m2525bAffiliation}${m2525bBattleDim}P${m2525bFuncId}-----"
    }

    fun from2525B(sidc : String) : String {
        require(sidc.matches(Regex("^S[PUAFNSHGWMDLJK\\-][PAGSUFXZ\\-][AP\\-][A-Z0-9\\-]{10}[AECGNS\\-]$"))){"2525B to CoT can only be applied to well-formed warfighting 2525B SIDCs."}
        val cotAffiliation = when (val m2525bAffiliation = sidc.substring(1,2)) {
            "G","W","M","D","L" -> "f"
            "-" -> "o"
            else -> m2525bAffiliation.lowercase()
        }
        val cotBattleDim = sidc.substring(2,3)
        val cotFuncId = StringBuilder()
        sidc.substring(4,10).replace(Regex("[^A-Z0-9]+"), "").forEach { x ->
            cotFuncId.append("-").append(x)
        }
        return "a-${cotAffiliation}-${cotBattleDim}${cotFuncId}"
    }

}
