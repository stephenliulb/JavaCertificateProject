/*
 * Basic Java skill show cases
 *
 * Copyright (c) 2024 Stephen Liu. All Rights Reserved. 
 *
 */

package stephen.ui.view;

import java.awt.event.ActionListener;
import java.util.LinkedHashMap;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * This class provides the interface to easily manage the process of menu
 * creation.
 * <p>
 * Basically, each menu bar has a list of menu items and each menu item has its
 * name and corresponding ActionListener object; This class effectively
 * organizes all these information and return a fully initialized JMenuBar
 * object.
 * 
 * @author Stephen Liu
 * 
 */
public class MenuModel {

	private LinkedHashMap<String, MenuItemModel[]> menus = new LinkedHashMap<String, MenuItemModel[]>();

	/**
	 * Add one menu and all its menu items.
	 * 
	 * @param menuName  menu name.
	 * @param menuItems menu items belonged to the menu.
	 */
	public void add(String menuName, MenuItemModel... menuItems) {
		menus.put(menuName, menuItems);
	}

	/**
	 * Get JmenuBar object which include all menus and menu items.
	 * 
	 * @return a menu bar object
	 */
	public JMenuBar getMenuBar() {
		JMenuBar mb = new JMenuBar();

		for (String menuName : menus.keySet()) {
			JMenu mu = new JMenu(menuName);

			MenuItemModel[] miArry = menus.get(menuName);
			for (MenuItemModel mm : miArry) {
				JMenuItem mi = new JMenuItem(mm.menuItemName);
				mi.addActionListener(mm.listener);
				mu.add(mi);
			}
			mb.add(mu);
		}

		return mb;
	}

	/**
	 * This class describes the menu item structure which is composed of menu item
	 * name and ActionListener object.
	 * 
	 * @author Stephen Liu
	 * 
	 */
	public class MenuItemModel {
		final String menuItemName;
		final ActionListener listener;

		/**
		 * Create an object for a menu item.
		 * 
		 * @param menuItemName
		 * @param listener
		 */
		MenuItemModel(String menuItemName, ActionListener listener) {
			this.menuItemName = menuItemName;
			this.listener = listener;
		}
	}
}
