package com.happyzleaf.serverguis.data;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author happyzleaf
 * @since 13/08/2018
 */
public class GuiData {
	private static ConfigurationLoader<CommentedConfigurationNode> loader;
	private static CommentedConfigurationNode node;
	
	public static List<Gui> guiList = new ArrayList<>();
	
	public static void init(File file) {
		loader = HoconConfigurationLoader.builder().setFile(file).build();
		loadNode();
	}
	
	public static void loadNode() {
		load();
		try {
			guiList = new ArrayList<>(node.getNode("list").getList(TypeToken.of(Gui.class)));
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveNode() {
		load();
		try {
			node.getNode("list").setValue(new TypeToken<List<Gui>>() {}, guiList);
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		save();
	}
	
	private static void load() {
		try {
			node = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void save() {
		try {
			loader.save(node);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
