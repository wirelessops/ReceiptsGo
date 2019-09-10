package wb.android.flex;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

class FlexViews {
	
	private static final String TAG = "FlexViews";
	private static final boolean D = Flex.D;
	
	private final Stack<String> ids;
	private final Stack<FlexViews.Element> elements;
	private final Stack<List<FlexView>> views;
	private final HashMap<String, FlexView> viewMap;
	private final HashMap<String, List<FlexView>> viewGroupChildren;
	private final FlexViews.Element[] elementCache;
	
	enum Element {
		EDIT_TEXT ("EditText", EditText.class),
		CHECKBOX ("CheckBox", CheckBox.class),
		SPINNER ("Spinner", Spinner.class),
		AUTOCOMPLETE_TEXT_VIEW ("AutoCompleteTextView", AutoCompleteTextView.class),
		LINEAR_LAYOUT ("LinearLayout", LinearLayout.class);
		
		private final String name;
		private final Class<? extends View> viewClass;
		private final boolean isViewGroup;
		private Element(String name, Class<? extends View> c) {this.name = name; this.viewClass = c; this.isViewGroup = (ViewGroup.class.isAssignableFrom(viewClass));}
		boolean isElement(String localName) {return this.name.equalsIgnoreCase(localName); }
		boolean isViewGroup() { return isViewGroup; }
		String elementName() { return name; }
		Class<? extends View> getViewClass() { return viewClass; }
	}
	
	enum Attribute {
		CHECKED ("android:checked"), 
		HINT ("android:hint"),
		ID ("android:id"),
		LAYOUT_HEIGHT ("android:layout_height"),
		LAYOUT_WEIGHT ("android:layout_weight"),
		LAYOUT_WIDTH ("android:layout_width"),
		TAG ("android:tag"),
		VISIBILITY ("android:visibility"), 
		TEXT ("android:text");
		
		private String name;
		private Attribute(String name) {this.name = name;}
		boolean isTag(String localName) {return this.name.equalsIgnoreCase(localName); }
		String tagName() { return this.name; }
	}
	
	FlexViews() {
		this.ids = new Stack<String>();
		this.elements = new Stack<FlexViews.Element>();
		this.views = new Stack<List<FlexView>>();
		this.viewMap = new HashMap<String, FlexView>();
		this.viewGroupChildren = new HashMap<String, List<FlexView>>();
		this.elementCache = FlexViews.Element.values();
	} 
	
	void startElement(Attributes attributes, String localName) throws SAXException {
		FlexViews.Element element = null;
		for (int i=0; i<elementCache.length; i++) {
			if (elementCache[i].isElement(localName)) {
				element = elementCache[i];
				break;
			}
		}
		if (element == null) 
			throw new SAXException("Undefined FlexViews Element: " + localName);
		elements.push(element);
		String id = attributes.getValue(FlexViews.Attribute.ID.tagName());
		if (element.isViewGroup()) {
			if (id == null) 
				throw new SAXException("All ViewGroups require that a \"" + FlexViews.Attribute.ID.tagName() + "\" attribute is defined.");
			ids.push(id);
			views.push(new ArrayList<FlexView>());
		}
		else {
			if (!views.isEmpty()) 
				views.peek().add(new FlexView(attributes, element));
		}
		if (id != null) {
			if (!viewMap.containsKey(id))
				viewMap.put(id, new FlexView(attributes, element));
			else
				throw new SAXException("Views cannot have the same ID: " + id);
		}
	}
	
	void endElement(String localName) throws SAXException {
		FlexViews.Element element = null;
		for (int i=0; i<elementCache.length; i++) {
			if (elementCache[i].isElement(localName)) {
				element = elementCache[i];
				break;
			}
		}
		if (element == null) 
			throw new SAXException("Undefined FlexViews Element: " + localName);
		if (elements.pop() != element)
			throw new SAXException("The end XML tag for " + localName + " did not match the expected tag: " + element.elementName());
		if (element.isViewGroup()) 
			viewGroupChildren.put(ids.pop(), views.pop());
	}
	
	boolean updateView(View view, String id) throws FlexFailedException {
		if (id == null) return false;
		if (viewMap.containsKey(id)) {
			if (D) Log.d(TAG, "Updating View - ID: " + id);
			viewMap.get(id).update(view);
			return true;
		}
		return false;
	}
	
	boolean updateView(EditText editText, String id) throws FlexFailedException {
		if (id == null) return false;
		if (viewMap.containsKey(id)) {
			if (D) Log.d(TAG, "Updating EditText - ID: " + id);
			viewMap.get(id).update(editText);
			return true;
		}
		return false;
	}
	
	boolean updateView(CheckBox checkBox, String id) throws FlexFailedException {
		if (id == null) return false;
		if (viewMap.containsKey(id)) {
			if (D) Log.d(TAG, "Updating CheckBox - ID: " + id);
			viewMap.get(id).update(checkBox);
			return true;
		}
		return false;
	}
	
	boolean addFlexViewsToParent(Context context, ViewGroup parent, String parentID) throws FlexFailedException {
		if (parentID == null) return false;
		if (viewGroupChildren.containsKey(parentID)) {
			List<FlexView> views = viewGroupChildren.get(parentID);
			final int size = views.size();
			for(int i=0; i<size; i++) { views.get(i).buildAndAddToParent(context, parent); }
			return true;
		}
		else {
			return false;
		}
	}
	
}
