package org.processmining.ocel.utils;

import java.math.BigDecimal;
import java.util.Date;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;

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
		else if (value.getClass() == java.lang.Integer.class) {
			ret[0] = "float";
			ret[1] = ((Integer)value).toString();
		}
		else if (value.getClass() == java.util.Date.class) {
			ret[0] = "date";
			ret[1] = ((Date) value).toInstant().toString();
		}
		else {
			System.out.println("AAAA");
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
		else if (value0.getClass() == java.lang.Integer.class) {
			Integer value = (Integer)value0;
			ret = new XAttributeContinuousImpl(name, value);
		}
		else if (value0.getClass() == java.lang.String.class) {
			ret = new XAttributeLiteralImpl(name, value0.toString());
		}
		else if (value0.getClass() == java.util.Date.class) {
			ret = new XAttributeTimestampImpl(name, ((Date)value0));
		}
		else {
			System.out.println("BBBB");
			System.out.println(value0.getClass());
			ret = new XAttributeLiteralImpl(name, value0.toString());
		}
		return ret;
	}
}
