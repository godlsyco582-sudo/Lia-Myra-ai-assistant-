package com.Lia.assistant.agent.core

class AccessibilityScreenDriver(private val serviceProvider: () -> NovaAccessibilityService?) : ScreenDriver {

    override suspend fun capture(): ScreenObservation {
        val service = serviceProvider()
        val elements = mutableListOf<UiElement>()
        if (service != null) {
            val textList = service.readScreen()
            for ((idx, line) in textList.withIndex()) {
                elements.add(
                    UiElement(
                        id = "elem_$idx",
                        text = line,
                        className = "View",
                        isClickable = line.startsWith("[button]"),
                        isEditable = line.startsWith("[input]")
                    )
                )
            }
        }
        return ScreenObservation(
            observationId = "obs_${System.currentTimeMillis()}",
            packageName = "android",
            windowClass = "ActiveWindow",
            elements = elements,
            screenSize = Pair(1080, 2400),
            windowStamp = System.currentTimeMillis()
        )
    }

    override suspend fun clickElement(obs: ScreenObservation, element: UiElement): ScreenDriverResult {
        val service = serviceProvider() ?: return ScreenDriverResult.FAILED
        val clean = element.text.removePrefix("[button] ").removePrefix("[text] ").removePrefix("[input] ")
        val ok = service.tapText(clean)
        return if (ok) ScreenDriverResult.OK else ScreenDriverResult.NOT_FOUND
    }

    override suspend fun gestureTap(x: Float, y: Float): ScreenDriverResult {
        return ScreenDriverResult.UNSUPPORTED
    }

    override suspend fun setText(text: String): ScreenDriverResult {
        val service = serviceProvider() ?: return ScreenDriverResult.FAILED
        val ok = service.typeText(text)
        return if (ok) ScreenDriverResult.OK else ScreenDriverResult.FAILED
    }

    override suspend fun scroll(direction: String): ScreenDriverResult {
        val service = serviceProvider() ?: return ScreenDriverResult.FAILED
        val ok = service.scroll(direction)
        return if (ok) ScreenDriverResult.OK else ScreenDriverResult.FAILED
    }

    override suspend fun back(): ScreenDriverResult {
        val service = serviceProvider() ?: return ScreenDriverResult.FAILED
        val ok = service.goBack()
        return if (ok) ScreenDriverResult.OK else ScreenDriverResult.FAILED
    }

    override suspend fun home(): ScreenDriverResult {
        val service = serviceProvider() ?: return ScreenDriverResult.FAILED
        val ok = service.goHome()
        return if (ok) ScreenDriverResult.OK else ScreenDriverResult.FAILED
    }
}

class FakeScreenDriver(
    var currentObservation: ScreenObservation = ScreenObservation(
        observationId = "fake_obs_0",
        packageName = "com.instagram.android",
        windowClass = "MainActivity",
        elements = listOf(
            UiElement("btn_new_post", "New Post", "Button", isClickable = true, isEditable = false),
            UiElement("btn_share", "Share", "Button", isClickable = true, isEditable = false)
        ),
        screenSize = Pair(1080, 2400),
        windowStamp = 1000L
    )
) : ScreenDriver {

    var lastClickedElement: UiElement? = null
    var lastTypedText: String? = null
    var failClicks: Boolean = false

    override suspend fun capture(): ScreenObservation = currentObservation

    override suspend fun clickElement(obs: ScreenObservation, element: UiElement): ScreenDriverResult {
        if (obs.windowStamp != currentObservation.windowStamp) {
            return ScreenDriverResult.STALE
        }
        if (failClicks) return ScreenDriverResult.FAILED
        lastClickedElement = element
        return ScreenDriverResult.OK
    }

    override suspend fun gestureTap(x: Float, y: Float): ScreenDriverResult = ScreenDriverResult.OK

    override suspend fun setText(text: String): ScreenDriverResult {
        lastTypedText = text
        return ScreenDriverResult.OK
    }

    override suspend fun scroll(direction: String): ScreenDriverResult = ScreenDriverResult.OK

    override suspend fun back(): ScreenDriverResult = ScreenDriverResult.OK

    override suspend fun home(): ScreenDriverResult = ScreenDriverResult.OK
}
