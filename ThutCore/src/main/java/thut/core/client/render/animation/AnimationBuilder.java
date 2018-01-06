package thut.core.client.render.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import thut.core.client.render.animation.AnimationRegistry.IPartRenamer;
import thut.core.client.render.tabula.components.Animation;
import thut.core.client.render.tabula.components.AnimationComponent;

public class AnimationBuilder
{
    /** Constructs a new Animation, and assigns components based on the
     * definitions in the XML node.
     * 
     * @param node
     * @param renamer
     * @return */
    public static Animation build(Node node, @Nullable IPartRenamer renamer)
    {
        Animation ret = null;
        if (node.getAttributes().getNamedItem("type") == null) { return null; }
        String animName = node.getAttributes().getNamedItem("type").getNodeValue();

        ret = new Animation();
        ret.name = animName;
        ret.loops = true;
        if (node.getAttributes().getNamedItem("loops") != null)
        {
            ret.loops = Boolean.parseBoolean(node.getAttributes().getNamedItem("loops").getNodeValue());
        }

        NodeList parts = node.getChildNodes();
        Node temp;
        for (int i = 0; i < parts.getLength(); i++)
        {
            Node part = parts.item(i);
            if (part.getNodeName().equals("part"))
            {
                NodeList components = part.getChildNodes();
                String partName = part.getAttributes().getNamedItem("name").getNodeValue();
                if (renamer != null)
                {
                    String[] names = { partName };
                    renamer.convertToIdents(names);
                    partName = names[0];
                }
                ArrayList<AnimationComponent> set = Lists.newArrayList();
                for (int j = 0; j < components.getLength(); j++)
                {
                    Node component = components.item(j);
                    if (component.getNodeName().equals("component"))
                    {
                        AnimationComponent comp = new AnimationComponent();
                        if ((temp = component.getAttributes().getNamedItem("name")) != null)
                        {
                            comp.name = temp.getNodeValue();
                        }
                        if ((temp = component.getAttributes().getNamedItem("rotChange")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.rotChange[0] = Double.parseDouble(vals[0]);
                            comp.rotChange[1] = Double.parseDouble(vals[1]);
                            comp.rotChange[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("posChange")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.posChange[0] = Double.parseDouble(vals[0]);
                            comp.posChange[1] = Double.parseDouble(vals[1]);
                            comp.posChange[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("scaleChange")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.scaleChange[0] = Double.parseDouble(vals[0]);
                            comp.scaleChange[1] = Double.parseDouble(vals[1]);
                            comp.scaleChange[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("rotOffset")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.rotOffset[0] = Double.parseDouble(vals[0]);
                            comp.rotOffset[1] = Double.parseDouble(vals[1]);
                            comp.rotOffset[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("posOffset")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.posOffset[0] = Double.parseDouble(vals[0]);
                            comp.posOffset[1] = Double.parseDouble(vals[1]);
                            comp.posOffset[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("scaleOffset")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.scaleOffset[0] = Double.parseDouble(vals[0]);
                            comp.scaleOffset[1] = Double.parseDouble(vals[1]);
                            comp.scaleOffset[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("length")) != null)
                        {
                            comp.length = Integer.parseInt(temp.getNodeValue());
                        }
                        if ((temp = component.getAttributes().getNamedItem("startKey")) != null)
                        {
                            comp.startKey = Integer.parseInt(temp.getNodeValue());
                        }
                        if ((temp = component.getAttributes().getNamedItem("opacityChange")) != null)
                        {
                            comp.opacityChange = Double.parseDouble(temp.getNodeValue());
                        }
                        if ((temp = component.getAttributes().getNamedItem("opacityOffset")) != null)
                        {
                            comp.opacityOffset = Double.parseDouble(temp.getNodeValue());
                        }
                        if ((temp = component.getAttributes().getNamedItem("hidden")) != null)
                        {
                            comp.hidden = Boolean.parseBoolean(temp.getNodeValue());
                        }
                        set.add(comp);
                    }
                }
                if (!set.isEmpty())
                {
                    ret.sets.put(partName, set);
                }
            }
        }
        return ret;
    }

    public static void processAnimations(List<Animation> list)
    {
        List<Animation> oldList = Lists.newArrayList(list);
        Map<Integer, List<Animation>> splitAnims = Maps.newHashMap();
        for (Animation anim : oldList)
        {
            splitAnimation(anim, splitAnims);
        }
        list.clear();
        for (List<Animation> split : splitAnims.values())
        {
            list.add(mergeAnimations(split));
        }
    }

    private static void splitAnimation(Animation animIn, Map<Integer, List<Animation>> fill)
    {
        for (Entry<String, ArrayList<AnimationComponent>> entry : animIn.sets.entrySet())
        {
            String key = entry.getKey();
            ArrayList<AnimationComponent> comps = entry.getValue();
            int length = length(comps);
            List<Animation> anims = fill.get(length);
            if (anims == null) fill.put(length, anims = Lists.newArrayList());
            Animation newAnim = new Animation();
            newAnim.name = animIn.name;
            newAnim.identifier = animIn.identifier;
            newAnim.loops = animIn.loops;
            newAnim.priority = animIn.priority;
            newAnim.sets.put(key, comps);
            anims.add(newAnim);
        }
    }

    private static void addTo(Animation animation, int priority, String part, ArrayList<AnimationComponent> parts)
    {
        if (animation.sets.containsKey(part) && animation.priority > priority)
        {
            System.err.println("Already have " + part + ", Skipping.");
        }
        else animation.sets.put(part, parts);
    }

    private static Animation mergeAnimations(List<Animation> list)
    {
        if (list.isEmpty()) return null;
        Animation newAnim = new Animation();
        newAnim.name = list.get(0).name;
        newAnim.identifier = list.get(0).identifier;
        newAnim.loops = list.get(0).loops;
        newAnim.priority = list.get(0).priority;
        for (Animation anim : list)
        {
            for (String part : anim.sets.keySet())
            {
                addTo(newAnim, anim.priority, part, anim.sets.get(part));
            }
        }
        return newAnim;
    }

    private static int length(List<AnimationComponent> comps)
    {
        int length = 0;
        for (AnimationComponent comp : comps)
            length = Math.max(length, comp.startKey + comp.length);
        return length;
    }
}
