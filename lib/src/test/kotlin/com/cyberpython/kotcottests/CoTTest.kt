package com.cyberpython.kotcottests

import com.cyberpython.kotcot.CoT
import com.cyberpython.kotcot.CoTType2SIDC
import com.cyberpython.kotcot.Event
import com.cyberpython.kotcot.Point
import java.time.ZonedDateTime
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull


class CoTTest {
    @Test fun baseEventToXml() {
        val cot = CoT()
        
        val e1 = Event(Point(37.409, 23.768, 0.0, 0.0, 0.0), null, 2, CoTType2SIDC().from2525B("SFGPUCVRA------"), "public", "1-r-c", "4333319d-3430-44f3-bada-195fcc9bf349", ZonedDateTime.parse("2022-08-20T18:50:10Z"), ZonedDateTime.parse("2022-08-20T18:50:10Z"), ZonedDateTime.parse("2022-08-20T19:00:10Z"), "m-g")
        val xml = cot.write(e1)

        val builderFactory = DocumentBuilderFactory.newInstance()
        val builder = builderFactory.newDocumentBuilder()
        val xmlDocument = builder.parse(xml.byteInputStream())
        val xPath = XPathFactory.newInstance().newXPath()
        assertEquals("2", xPath.compile("/event[1]/@version").evaluate(xmlDocument, XPathConstants.STRING))
        assertEquals("a-f-G-U-C-V-R-A", xPath.compile("/event[1]/@type").evaluate(xmlDocument, XPathConstants.STRING))
        assertEquals("public", xPath.compile("/event[1]/@access").evaluate(xmlDocument, XPathConstants.STRING))
        assertEquals("1-r-c", xPath.compile("/event[1]/@qos").evaluate(xmlDocument, XPathConstants.STRING))
        assertEquals("4333319d-3430-44f3-bada-195fcc9bf349", xPath.compile("/event[1]/@uid").evaluate(xmlDocument, XPathConstants.STRING))
        assertEquals("2022-08-20T18:50:10Z", xPath.compile("/event[1]/@time").evaluate(xmlDocument, XPathConstants.STRING))
        assertEquals("2022-08-20T18:50:10Z", xPath.compile("/event[1]/@start").evaluate(xmlDocument, XPathConstants.STRING))
        assertEquals("2022-08-20T19:00:10Z", xPath.compile("/event[1]/@stale").evaluate(xmlDocument, XPathConstants.STRING))
        assertEquals("m-g", xPath.compile("/event[1]/@how").evaluate(xmlDocument, XPathConstants.STRING))
        assertEquals("37.409", xPath.compile("/event[1]/point[1]/@lat").evaluate(xmlDocument, XPathConstants.STRING))
        assertEquals("23.768", xPath.compile("/event[1]/point[1]/@lon").evaluate(xmlDocument, XPathConstants.STRING))
        assertEquals("0.0", xPath.compile("/event[1]/point[1]/@hae").evaluate(xmlDocument, XPathConstants.STRING))
        assertEquals("0.0", xPath.compile("/event[1]/point[1]/@ce").evaluate(xmlDocument, XPathConstants.STRING))
        assertEquals("0.0", xPath.compile("/event[1]/point[1]/@le").evaluate(xmlDocument, XPathConstants.STRING))
    }

    @Test fun xmlToBaseEvent() {
        val xml = """
        <event version="2" type="a-h-S-C-L-D-D" access="national-restricted" qos="5-r-c" uid="1f3836bf-67b0-447c-8f41-292a325112b7" time="2022-08-19T16:09:11.128558Z" start="2022-08-19T16:09:11.130456Z" stale="2022-08-19T16:19:11.130479Z" how="m-g">
          <point lat="37.409" lon="23.768" hae="0.0" ce="0.0" le="0.0"/>
        </event>
        """.trimIndent()

        val cot = CoT()
        val evt = cot.parse(xml)
        assertEquals(2, evt.version)
        assertEquals("national-restricted", evt.access)
        assertEquals("5-r-c", evt.qos)
        assertEquals("a-h-S-C-L-D-D", evt.type)
        assertEquals(null, evt.detail)
        assertEquals("1f3836bf-67b0-447c-8f41-292a325112b7", evt.uid)
        assertEquals(ZonedDateTime.parse("2022-08-19T16:09:11.128558Z[UTC]"), evt.time)
        assertEquals(ZonedDateTime.parse("2022-08-19T16:09:11.130456Z[UTC]"), evt.start)
        assertEquals(ZonedDateTime.parse("2022-08-19T16:19:11.130479Z[UTC]"), evt.stale)
        assertEquals("m-g", evt.how)
        assertEquals(37.409, evt.point.lat, 0.001)
        assertEquals(23.768, evt.point.lon, 0.001)
        assertEquals(0.0, evt.point.hae, 0.001)
        assertEquals(0.0, evt.point.ce, 0.001)
        assertEquals(0.0, evt.point.le, 0.001)
    }

    @Test fun xmlToEventWithDetail() {
        val xml = """
        <event version="2" type="a-f-S-C-L-D-D" access="national-restricted" qos="5-r-c" uid="1f3836bf-67b0-447c-8f41-292a325112c8" time="2022-08-19T16:09:11.128558Z" start="2022-08-19T16:09:11.130456Z" stale="2022-08-19T16:19:11.130479Z" how="m-g">
          <point lat="37.409" lon="23.768" hae="0.0" ce="0.0" le="0.0"/>
          <detail>
            <vessel name="HS Themistocles" class="Charles F. Adams"/>
            <kinematic>
                <speed>12.5</speed>
                <course>136.8</course>
            </kinematic>
          </detail>
        </event>
        """.trimIndent()

        val cot = CoT()
        val evt = cot.parse(xml)
        assertEquals(2, evt.version)
        assertEquals("national-restricted", evt.access)
        assertEquals("5-r-c", evt.qos)
        assertEquals("a-f-S-C-L-D-D", evt.type)
        assertNotNull(evt.detail)
        assertIs<Map<String, Any>>(evt.detail)

        assertEquals("HS Themistocles", evt.getDetail("vessel.name"))
        assertEquals("Charles F. Adams", evt.getDetail("vessel.class"))
        assertEquals("12.5", evt.getDetail("kinematic.speed"))
        assertEquals("136.8", evt.getDetail("kinematic.course"))

        assertEquals("1f3836bf-67b0-447c-8f41-292a325112c8", evt.uid)
        assertEquals(ZonedDateTime.parse("2022-08-19T16:09:11.128558Z[UTC]"), evt.time)
        assertEquals(ZonedDateTime.parse("2022-08-19T16:09:11.130456Z[UTC]"), evt.start)
        assertEquals(ZonedDateTime.parse("2022-08-19T16:19:11.130479Z[UTC]"), evt.stale)
        assertEquals("m-g", evt.how)
        assertEquals(37.409, evt.point.lat, 0.001)
        assertEquals(23.768, evt.point.lon, 0.001)
        assertEquals(0.0, evt.point.hae, 0.001)
        assertEquals(0.0, evt.point.ce, 0.001)
        assertEquals(0.0, evt.point.le, 0.001)
    }

    @Test fun cotTypeToMilStd2525B() {
        assertEquals("SHSPCLDD-------", CoTType2SIDC().to2525B("a-h-S-C-L-D-D"))
        assertEquals("SFGPUCVRA------", CoTType2SIDC().to2525B("a-f-G-U-C-V-R-A"))
    }

    @Test(expected = IllegalArgumentException::class) fun invalidCotTypeToMilStd2525B_1() {
        CoTType2SIDC().to2525B("b-h-S-C-L-D-D")
    }

    @Test(expected = IllegalArgumentException::class) fun invalidCotTypeToMilStd2525B_2() {
        CoTType2SIDC().to2525B("")
    }

    @Test(expected = IllegalArgumentException::class) fun invalidCotTypeToMilStd2525B_3() {
        CoTType2SIDC().to2525B("bhSCLDD")
    }

    @Test(expected = IllegalArgumentException::class) fun invalidCotTypeToMilStd2525B_4() {
        CoTType2SIDC().to2525B("b-h-s-c-l-d-d")
    }

    @Test(expected = IllegalArgumentException::class) fun invalidCotTypeToMilStd2525B_5() {
        CoTType2SIDC().to2525B("b-h-S-?-L-D-D")
    }

    @Test fun milStd2525BToCotType() {
        assertEquals("a-h-S-C-L-D-D", CoTType2SIDC().from2525B("SHSPCLDD-------"))
        assertEquals("a-f-G-U-C-V-R-A", CoTType2SIDC().from2525B("SFGPUCVRA------"))
    }

    @Test(expected = IllegalArgumentException::class) fun invalidMilStd2525BToCotType_1() {
        CoTType2SIDC().to2525B("SFGPUCVRA")
    }

    @Test(expected = IllegalArgumentException::class) fun invalidMilStd2525BToCotType_2() {
        CoTType2SIDC().to2525B("SOGPUCVRA------")
    }

    @Test(expected = IllegalArgumentException::class) fun invalidMilStd2525BToCotType_3() {
        CoTType2SIDC().to2525B("SFMPUCVRA------")
    }

    @Test(expected = IllegalArgumentException::class) fun invalidMilStd2525BToCotType_4() {
        CoTType2SIDC().to2525B("SFMPUCVRA------")
    }

    @Test(expected = IllegalArgumentException::class) fun invalidMilStd2525BToCotType_5() {
        CoTType2SIDC().to2525B("SFGP*CVRA------")
    }

    @Test(expected = IllegalArgumentException::class) fun invalidMilStd2525BToCotType_6() {
        CoTType2SIDC().to2525B("GFGPUCVRA------")
    }
}
