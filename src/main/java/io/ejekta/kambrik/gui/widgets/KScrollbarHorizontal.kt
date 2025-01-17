package io.ejekta.kambrik.gui.widgets

import io.ejekta.kambrik.gui.KGuiDsl
import io.ejekta.kambrik.gui.KSpriteGrid

class KScrollbarHorizontal(
    scrollWidth: Int,
    knobSprite: KSpriteGrid.Sprite,
    backgroundColor: Int? = null
) : KScrollbar(knobSprite, backgroundColor) {
    override val height = knobSprite.height
    override val width = scrollWidth

    override val scrollbarSize: Int
        get() = width

    override val knobSize: Int
        get() = knobSprite.width

    override fun onDraw(dsl: KGuiDsl) = dsl {
        super.onDraw(this)
        val relX = dsl.mouseX - dsl.ctx.absX()
        val newPos = knobPos(relX)

        if (isDragging) {
            sprite(knobSprite, x = newPos)
            dragStart = newPos
        } else {
            sprite(knobSprite, x = dragStart)
        }
    }
}