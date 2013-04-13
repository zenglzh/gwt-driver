package org.senchalabs.gwt.gwtdriver.models;

/*
 * #%L
 * gwt-driver
 * %%
 * Copyright (C) 2012 - 2013 Sencha Labs
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.senchalabs.gwt.gwtdriver.invoke.ClientMethodsFactory;
import org.senchalabs.gwt.gwtdriver.invoke.ExportedMethods;
import org.senchalabs.gwt.gwtdriver.models.GwtWidget.ForWidget;

import com.google.gwt.user.client.ui.Widget;

/**
 * Represents a GWT Widget class, allowing some 
 * @author colin
 *
 */
@ForWidget(Widget.class)
public class GwtWidget<F extends GwtWidgetFinder<?>> {
	private final WebDriver driver;
	private final WebElement element;

	public GwtWidget(WebDriver driver, WebElement element) {
		assert element != null && driver != null;
		this.driver = driver;
		this.element = element;
	}

	public WebElement getElement() {
		return element;
	}
	public WebDriver getDriver() {
		return driver;
	}

	public <W extends GwtWidget<T>, T extends GwtWidgetFinder<W>> T find(Class<W> widgetType) {
		return find(widgetType, getDriver(), getElement());
	}
	public static <W extends GwtWidget<T>, T extends GwtWidgetFinder<W>> T find(Class<W> widgetType, WebDriver driver) {
		return find(widgetType, driver, null);
	}
	public static <W extends GwtWidget<T>, T extends GwtWidgetFinder<W>> T find(Class<W> widgetType, WebDriver driver, WebElement element) {
		Type i = widgetType;
		do {
			if (i instanceof ParameterizedType) {
				ParameterizedType t = (ParameterizedType) i;
				if (t.getRawType() == GwtWidget.class) {
					@SuppressWarnings("unchecked")
					Class<T> finderType = (Class<T>) t.getActualTypeArguments()[0];

					T instance = createInstance(finderType);
					if (instance != null) {
						instance.withDriver(driver);
						instance.withElement(element);
						return instance;
					}
				}
			}
			i = (i instanceof Class) ? ((Class<?>)i).getGenericSuperclass() : null;
		} while (i != null);
		return null;
	}

	protected static <T extends GwtWidgetFinder<?>> T createInstance(Class<T> type) {
		try {
			return type.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Declares that the annotated type is a GwtWidget for a particular Widget subclass. Allows
	 * multiple GwtWidget models to target the same client widget type, adding different features.
	 *
	 */
	@Documented
	@Inherited
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ForWidget {
		Class<? extends Widget> value();
	}

	public <W extends GwtWidget<?>> W as(Class<W> clazz) {
		try {
			W instance;
			ForWidget widgetType = clazz.getAnnotation(ForWidget.class);
			if (widgetType != null) {
				ExportedMethods m = ClientMethodsFactory.create(ExportedMethods.class, driver);
				String is = m.instanceofwidget(element, widgetType.value().getName());
				if (!"true".equals(is)) {
					throw new IllegalArgumentException("Cannot complete as(" + clazz.getSimpleName() + ".class), element isn't a " + widgetType.value().getName());
				}
			}
			instance = clazz.getConstructor(WebDriver.class, WebElement.class).newInstance(getDriver(), getElement());
			return instance;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * Helper method to generate a string literal that can be used in an xpath
	 * @param str
	 * @return a properly escaped string 
	 */
	protected static String escapeToString(String str) {
		if (!str.contains("'")) {
			return "'" + str + "'";
		}
		if (!str.contains("\"")) {
			return "\"" + str + "\"";
		}
		return "concat('" + str.replace("\'", "',\"'\",'") +"')";
	}
}