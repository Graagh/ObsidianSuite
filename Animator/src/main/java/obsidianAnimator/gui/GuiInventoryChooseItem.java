package obsidianAnimator.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import obsidianAnimator.data.ModelHandler;
import obsidianAnimator.gui.timeline.swing.subsection.TimelineItemController;
import obsidianAnimator.render.entity.EntityObj;

@SideOnly(Side.CLIENT)
public class GuiInventoryChooseItem extends GuiInventory
{

    private final boolean leftHand;
    private TimelineItemController controller;
	private EntityObj entity;

    public GuiInventoryChooseItem(boolean leftHand, TimelineItemController controller, EntityObj entity)
    {
        super(Minecraft.getMinecraft().player);
        this.allowUserInput = true;
        this.controller = controller;
        this.entity = entity;
        this.leftHand = leftHand;
        this.func_194310_f().func_194303_a(this.width, this.height, Minecraft.getMinecraft(), false, ((ContainerPlayer)this.inventorySlots).craftMatrix);
    }
    
    @Override
    public void initGui()
    {
    	super.initGui();
        this.buttonList.clear();
        this.mc.displayGuiScreen(new GuiContainerChooseItem(this.mc.player, this));
    }
    
    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
    	this.mc.displayGuiScreen(new GuiContainerChooseItem(this.mc.player, this));
    }
    
    public void setItemStack(ItemStack itemStack)
    {
        if (leftHand)
            ModelHandler.modelRenderer.setLeftItem(itemStack);
        else
    	    this.entity.setCurrentItem(itemStack);
    	controller.display();
    }
	

}