/*
 * Created on May 1, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package context.arch.logging;

import java.util.ArrayList;

/**
 * @author Marti Motoyama
 *
 */
public class LocationFilterListModel extends HibernateListModel{

	public LocationFilterListModel(){
		list = new ArrayList<String>();
		list.add("Pending");
		this.defaultQuery = "select distinct wra.attributevaluestring from WRAttribute wra where wra.attributename = 'location'";
		this.setListToDefaultQuery();
	}
	
}
