package io.ejekta.bountiful.common.content.gui

import io.ejekta.bountiful.common.content.BountifulContent
import io.ejekta.bountiful.common.content.board.BoardBlock
import net.minecraft.item.ItemStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot


class BoardScreenHandler @JvmOverloads constructor(
    syncId: Int,
    playerInventory: PlayerInventory,
    inventory: Inventory = SimpleInventory(BoardBlock.SIZE)
) : ScreenHandler(BountifulContent.BOARD_SCREEN_HANDLER, syncId) {

    private val inventory: Inventory
    override fun canUse(player: PlayerEntity): Boolean {
        return inventory.canPlayerUse(player)
    }

    // Shift + Player Inv Slot
    override fun transferSlot(player: PlayerEntity, invSlot: Int): ItemStack {
        var newStack = ItemStack.EMPTY
        val slot: Slot? = slots[invSlot]
        if (slot != null && slot.hasStack()) {
            val originalStack: ItemStack = slot.stack
            newStack = originalStack.copy()
            if (invSlot < inventory.size()) {
                if (!insertItem(originalStack, inventory.size(), slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!insertItem(originalStack, 0, inventory.size(), false)) {
                return ItemStack.EMPTY
            }
            if (originalStack.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
        }
        return newStack
    }

    //This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
    //and can therefore directly provide it as an argument. This inventory will then be synced to the client.
    //This constructor gets called on the client when the server wants it to open the screenHandler,
    //The client will call the other constructor with an empty Inventory and the screenHandler will automatically
    //sync this empty inventory with the inventory on the server.
    init {
        checkSize(inventory, BoardBlock.SIZE)
        this.inventory = inventory
        //some inventories do custom logic when a player opens it.
        inventory.onOpen(playerInventory.player)

        val bRows = 3
        val bCols = 7

        for (j in 0 until bRows) {
            for (k in 0 until bCols) {
                addSlot(BoardBountySlot(inventory, k + j * bCols, 8 + k * 18, 18 + j * 18))
            }

            addSlot(BoardDecreeSlot(inventory, inventory.size() - 3 + j, 19 + 7 * 18, 18 + j * 18))
        }

        //This will place the slot in the correct locations for a 3x3 Grid. The slots exist on both server and client!
        //This will not render the background of the slots however, this is the Screens job

        //The player inventory
        var m = 0
        var l = 0
        while (m < 3) {
            l = 0
            while (l < 9) {
                addSlot(Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18))
                ++l
            }
            ++m
        }
        //The player Hotbar
        m = 0
        while (m < 9) {
            addSlot(Slot(playerInventory, m, 8 + m * 18, 142))
            ++m
        }
    }
}
