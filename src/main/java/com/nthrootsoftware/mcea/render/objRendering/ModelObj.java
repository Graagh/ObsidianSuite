package com.nthrootsoftware.mcea.render.objRendering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;
import com.nthrootsoftware.mcea.Util;
import com.nthrootsoftware.mcea.animation.AnimationParenting;
import com.nthrootsoftware.mcea.animation.PartGroups;
import com.nthrootsoftware.mcea.render.objRendering.bend.Bend;
import com.nthrootsoftware.mcea.render.objRendering.parts.Part;
import com.nthrootsoftware.mcea.render.objRendering.parts.PartEntityPos;
import com.nthrootsoftware.mcea.render.objRendering.parts.PartObj;
import com.nthrootsoftware.mcea.render.objRendering.parts.PartRotation;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelFormatException;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.client.model.obj.WavefrontObject;

public class ModelObj extends ModelBase
{

	public final String entityName;
	public WavefrontObject model;
	public ArrayList<Part> parts;
	private ArrayList<Bend> bends;
	public AnimationParenting parenting;
	public PartGroups partGroups;
	private Map<PartObj, float[]> defaults;

	private PartObj mainHighlight = null;
	private List<PartObj> hightlightedParts;

	public static final float initRotFix = 180.0F;
	public static final float offsetFixY = -1.5F;

	private final ResourceLocation txtRL;
	
	private boolean partSetupComplete;

	public static final ResourceLocation pinkResLoc = new ResourceLocation("mod_mcea:defaultModelTextures/pink.png");
	public static final ResourceLocation whiteResLoc = new ResourceLocation("mod_mcea:defaultModelTextures/white.png");

	public ModelObj(String entityName, File file)
	{			
		this.entityName = entityName;
		
		hightlightedParts = new ArrayList<PartObj>();
		bends = new ArrayList<Bend>();
		defaults = Maps.newHashMap();
		
		loadFromFile(file);
		
		txtRL = whiteResLoc;//DataHandler.getEntityResourceLocation(entityName);
				
		init();
	}

	public ResourceLocation getTexture()
	{
		return txtRL;
	}

	public void init() 
	{
		for(Part p : this.parts)
		{
			if(p instanceof PartObj)
			{
				PartObj obj = (PartObj) p;
				float[] arr = new float[3];
				arr[0] = obj.getRotationPoint(0);
				arr[1] = obj.getRotationPoint(1);
				arr[2] = obj.getRotationPoint(2);
				defaults.put(obj, arr);
			}
		}		
	}

	public float[] getDefaults(PartObj part)
	{
		return defaults.get(part).clone();
	}

	private void loadFromFile(File file) 
	{
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			String nbtData = "";
			String modelData = "";
			String partData = "";

			int targetString = 0;
			String currentLine = "";
			while((currentLine = reader.readLine()) != null)
			{				
				if(currentLine.contains("# Model #"))
					targetString = 1;
				else if(currentLine.contains("# Part #"))
					targetString = 2;
				else
				{
					switch(targetString)
					{
					case 0: nbtData += currentLine + "\n"; break;
					case 1: modelData += currentLine + "\n"; break;
					case 2: partData += currentLine + "\n"; break;
					}
				}
			}

			//Write nbt data to temp file so it can be read by compressed stream tools.
			File tmp = new File("tmp");
			if(!tmp.exists())
				tmp.createNewFile();
			
			FileWriter fw = new FileWriter(tmp);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(nbtData);
			bw.close();
			fw.close();
			
			loadModel(modelData);
			
			parts = createPartObjList(this, model.groupObjects);
			parts.add(new PartEntityPos(this));
			if(entityName.equals("player"))
			{
				parts.add(new PartRotation(this, "prop_rot"));
				parts.add(new Part(this, "prop_trans"));
			}
			
			loadAdditionalPartData(partData);
			loadSetup(CompressedStreamTools.read(tmp));
			
			reader.close();
			tmp.delete();
		} 
		catch (FileNotFoundException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}
	}
	
	private void loadModel(String modelData) throws ModelFormatException, UnsupportedEncodingException
	{
		model = new WavefrontObject("Test file", new ByteArrayInputStream(modelData.getBytes("UTF-8")));
	}
	
	private void loadAdditionalPartData(String partData)
	{
		PartObj currentPart = null;
		int i = 0;
		boolean readingRotation = true;

		for(String currentLine : partData.split("\n"))
		{
			if(currentLine.equals("PART SETUP"))
				readingRotation = false;

			if(readingRotation)
			{
				switch(i)
				{
				case 0: 
					for(Part p : parts)
					{
						if(p instanceof PartObj)
						{
							PartObj obj = (PartObj) p;
							if(obj.getName().equals(currentLine))
							{
								currentPart = obj;
								break;
							}
						}
					}
					i++;
					break;
				case 1:
					currentPart.setRotationPoint(read3Floats(currentLine));
					i++;
					break;
				case 2:
					float[] rot = read3Floats(currentLine);
					currentPart.setOriginalValues(rot);
					currentPart.setValues(rot);
					i = 0;
					break;
				}
			}
		}
	}
	
	private void loadSetup(NBTTagCompound nbt)
	{
		parenting = new AnimationParenting();
		parenting.loadData(nbt.getCompoundTag("Parenting"), this);

		partGroups = new PartGroups(this);
		partGroups.loadData(nbt.getCompoundTag("Groups"), this);
	}

	//----------------------------------------------------------------
	//				     Parts and Groups
	//----------------------------------------------------------------

	public List<PartObj> getPartObjs()
	{
		List<PartObj> partObjs = new ArrayList<PartObj>();
		for(Part part : parts)
		{
			if(part instanceof PartObj)
				partObjs.add((PartObj) part);
		}
		return partObjs;
	}

	public String getPartOrderAsString()
	{
		String s = "";
		for(PartObj p : getPartObjs())
		{
			s = s + p.getName() + ",";
		}
		s = s.substring(0, s.length() - 1);
		return s;
	}

	public void setPartOrderFromString(String order)
	{
		ArrayList<Part> newPartList = new ArrayList<Part>();
		for(String partName : order.split(","))
		{
			newPartList.add(Util.getPartFromName(partName, parts));
		}
		for(Part part : parts)
		{
			if(!newPartList.contains(part))
				newPartList.add(part);
		}
		parts = newPartList;
	}

	//----------------------------------------------------------------
	//						Parenting
	//----------------------------------------------------------------

	public void setParent(PartObj child, PartObj parent, boolean addBend) throws Exception
	{
		if(addBend)
		{			
			for(Part p : this.parts)
			{
				if(p instanceof PartObj)
				{
					PartObj obj = (PartObj) p;
					obj.updateTextureCoordinates(false, false, false);
				}
			}

			if(!child.hasBend())
			{
				Bend b = new Bend(parent, child);
				bends.add(b);
				child.setBend(b);
			}
		}
		parenting.addParenting(parent, child);
	}


	public void removeBend(Bend bend) 
	{
		bends.remove(bend);
	}


	//----------------------------------------------------------------
	//							Rotation
	//----------------------------------------------------------------

	public void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) 
	{				
		super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
	}

	//----------------------------------------------------------------
	// 							 Selection
	//----------------------------------------------------------------

	public PartObj testRay()
	{
		PartObj closestPart = null;
		Double min = null;
		for(Part part : parts)
		{
			if(part instanceof PartObj)
			{
				PartObj p = (PartObj) part;
				Double d = p.testRay();
				if(d != null && (min == null || d < min))
				{
					closestPart = p;
					min = d;
				}
			}
		}
				
		for(Bend bend : bends)
		{
			Double d = bend.testRayChild();
			if(d != null && (min == null || d < min))
			{
				closestPart = bend.child;
				min = d;
			}
			Double d2 = bend.testRayParent();
			if(d2 != null && (min == null || d2 < min))
			{
				closestPart = bend.parent;
				min = d2;
			}
		}
		return closestPart;
	}

	//----------------------------------------------------------------
	// 							Highlighting
	//----------------------------------------------------------------

	/**
	 * Highlight a part.
	 * @param part - Part to highlight.
	 * @param main - True if main highlight (pink).
	 */
	public void hightlightPart(PartObj part, boolean main)
	{
		if(part != null)
		{
			if(main)
				mainHighlight = part;
			else
				hightlightedParts.add(part);
		}
	}

	public void clearHighlights()
	{
		this.hightlightedParts.clear();
		this.mainHighlight = null;
	}

	public boolean isMainHighlight(PartObj partObj) 
	{
		return mainHighlight == partObj;
	}

	/**
	 * Highlighted but not main highlight (white).
	 */
	public boolean isPartHighlighted(PartObj partObj) 
	{
		return  hightlightedParts.contains(partObj);
	}

	//----------------------------------------------------------------
	//							Rendering
	//----------------------------------------------------------------

	@Override
	public void render(Entity entity, float time, float distance, float loop, float lookY, float lookX, float scale) 
	{		
		super.render(entity, time, distance, loop, lookY, lookX, scale);

		GL11.glPushMatrix();
		GL11.glRotatef(initRotFix, 1.0F, 0.0F, 0.0F);
		GL11.glTranslatef(0.0F, offsetFixY, 0.0F);

		for(Part p : this.parts) 
		{
			if(p instanceof PartObj)
			{
				PartObj part = (PartObj) p;
				if(!parenting.hasParent(part))
					part.render();
			}
			else if(p instanceof PartEntityPos)
				((PartEntityPos) p).move(entity);
		}

		for(Bend bend : this.bends)
			bend.render();

		GL11.glPopMatrix();
	}

	//----------------------------------------------------------------
	//							Utils
	//----------------------------------------------------------------

	private static float[] read3Floats(String str)
	{
		float[] arr = new float[3];
		for(int i = 0; i < 3; i++)
		{
			if(str.contains(","))
			{
				arr[i] = Float.parseFloat(str.substring(0, str.indexOf(",")));
				str = str.substring(str.indexOf(",") + 2);
			}
			else
			{
				arr[i] = Float.parseFloat(str);
			}
		}
		return arr;
	}

	public ArrayList<Part> createPartObjList(ModelObj model, ArrayList<GroupObject> groupObjects)
	{
		ArrayList<Part> parts = new ArrayList<Part>();
		for(GroupObject gObj : groupObjects)
		{
			parts.add(new PartObj(model, gObj));
		}
		return parts;
	}
	
	public NBTTagCompound createNBTTag()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setTag("Parenting", parenting.getSaveData());
		nbt.setTag("PartGroups", partGroups.getSaveData());
		return nbt;
	}

}