package org.processmining.ocel.utils;

import java.math.BigDecimal;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;

public class TypeFromValue {
	public static String[] getTypeStringForValue(Object value) {
		String[] ret = new String[2];
		if (value.getClass() == java.math.BigDecimal.class) {
			ret[0] = "float";
			ret[1] = ((java.math.BigDecimal)value).toString();
		}
		else if (value.getClass() == java.lang.Float.class) {
			ret[0] = "float";
			ret[1] = ((java.lang.Float)value).toString();
		}
		else if (value.getClass() == java.lang.String.class) {
			ret[0] = "string";
			ret[1] = value.toString();
		}
		else {
			System.out.println(value.getClass());
			ret[0] = "string";
			ret[1] = value.toString();
		}
		return ret;
	}
	
	public static XAttribute getAttributeForValue(String name, Object value0) {
		XAttribute ret = null;
		if (value0.getClass() == java.math.BigDecimal.class) {
			BigDecimal value = (BigDecimal)value0;
			ret = new XAttributeContinuousImpl(name, value.floatValue());
		}
		else if (value0.getClass() == java.lang.Float.class) {
			Float value = (Float)value0;
			ret = new XAttributeContinuousImpl(name, value);
		}
		else if (value0.getClass() == java.lang.String.class) {
			ret = new XAttributeLiteralImpl(name, value0.toString());
		}
		else {
			ret = new XAttributeLiteralImpl(name, value0.toString());
		}
		return ret;
	}
}
