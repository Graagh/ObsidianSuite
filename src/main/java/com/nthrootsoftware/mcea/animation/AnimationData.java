package com.nthrootsoftware.mcea.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.nthrootsoftware.mcea.render.objRendering.ModelObj;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/*
 * Contains all the data for animations and models.
 * 
 * Also holds information regarding the setup of GUIs (zoom, rotation, position etc.)
 */

public class AnimationData 
{	

	private static Map<String, AnimationParenting> parenting = Maps.newHashMap();
	private static List<String> changedEntitySetups = new ArrayList<String>();

	//Setup for GUIs
	private static Map<String, String> guiSetup = Maps.newHashMap();
	private static Map<String, Integer> animationItems = Maps.newHashMap();

	//List of part names and groupings. 
	private static Map<String, PartGroups> partGroups= Maps.newHashMap();

	/**
	 * Get the animation parenting that applies to this model.
	 * Will return a new one if one doesn't already exist.
	 */
	public static AnimationParenting getAnipar(String model) 
	{
		if(!parenting.containsKey(model) || parenting.get(model) == null)
			parenting.put(model, new AnimationParenting());
		return parenting.get(model);
	}

	public static void setEntitySetupChanged(String entityName)
	{
		changedEntitySetups.add(entityName);
	}

	public static boolean getEntitySetupChanged(String entityName)
	{
		return changedEntitySetups.contains(entityName);
	}

	public static void clearEntitySetupChanged()
	{
		changedEntitySetups.clear();
	}

	public static void setGUISetup(String entityName, String setup)
	{
		if(!setup.equals(""))
		{
			guiSetup.put(entityName, setup);
		}
	}

	public static String getGUISetup(String entityName)
	{

		return guiSetup.get(entityName);
	}

	public static PartGroups getPartGroups(String entityName, ModelObj model)
	{
		if(partGroups.containsKey(entityName) && partGroups.get(entityName) != null)
			return partGroups.get(entityName);
		PartGroups p = new PartGroups(model);
		partGroups.put(entityName, p);
		return p;
	}

	public static ItemStack getAnimationItem(String animationName)
	{
		Integer id;
		if((id = animationItems.get(animationName)) != null && id != -1)
		{
			Item item;
			Block block;
			if((item = Item.getItemById(id)) != null)
				return new ItemStack(item);
			else if((block = Block.getBlockById(id)) != null)
				return new ItemStack(block);
			else
				throw new RuntimeException("Unable to get item or block for id " + id);
		}
		return null;
	}

	public static void setAnimationItem(String animationName, int id)
	{
		animationItems.put(animationName, id);
	}

	public static NBTTagCompound getGUISetupTag(List<String> entities)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList entityList = new NBTTagList();
		for(String entity : entities)
		{ 
			NBTTagCompound guiSetupCompound = new NBTTagCompound();
			guiSetupCompound.setString("EntityName", entity);
			String setup = guiSetup.get(entity);
			if(setup != null && !setup.equals(""))
				guiSetupCompound.setString("GUISetup", setup);
			entityList.appendTag(guiSetupCompound);
		}
		nbt.setTag("GuiSetup", entityList);

		NBTTagList animationItemList = new NBTTagList();
		for(Entry<String, Integer> e : animationItems.entrySet())
		{
			NBTTagCompound animationItem = new NBTTagCompound();
			animationItem.setString("name", e.getKey());
			animationItem.setInteger("id", e.getValue());
			animationItemList.appendTag(animationItem);
		}
		nbt.setTag("AnimationItems", animationItemList);

		return nbt;
	}	

	public static void loadGUISetup(NBTTagCompound nbt)
	{
		System.out.println("Loading gui setup...");
		NBTTagList entityList = nbt.getTagList("GuiSetup", 10);
		for(int i = 0; i < entityList.tagCount(); i++)
		{
			NBTTagCompound guiSetupCompound = entityList.getCompoundTagAt(i);
			String entityName = guiSetupCompound.getString("EntityName");
			setGUISetup(entityName, guiSetupCompound.getString("GUISetup"));
		}

		NBTTagList animationItemList = nbt.getTagList("AnimationItems", 10);
		for(int i = 0; i < animationItemList.tagCount(); i++)
		{
			NBTTagCompound animationItem = animationItemList.getCompoundTagAt(i);
			setAnimationItem(animationItem.getString("name"), animationItem.getInteger("id"));
		}

		System.out.println(" Done");
	}	

	public static NBTTagCompound getEntityDataTag(String entityName) 
	{
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("Parenting", getAnipar(entityName).getSaveData(entityName));
		if(partGroups.get(entityName) != null)
			compound.setTag("Groups", partGroups.get(entityName).getSaveData(entityName));
		return compound;
	}

	public static void loadEntityData(String entityName, NBTTagCompound compound)
	{
//		AnimationParenting anipar = getAnipar(entityName);
//		anipar.loadData(compound.getCompoundTag("Parenting"), entityName);
//
//		PartGroups p = partGroups.get(entityName);
//		p.loadData(compound.getCompoundTag("Groups"), entityName);
	}

	public static void clear() 
	{
		parenting.clear();
		changedEntitySetups.clear();
		guiSetup.clear();
		animationItems.clear();
		partGroups.clear();
	}

}
