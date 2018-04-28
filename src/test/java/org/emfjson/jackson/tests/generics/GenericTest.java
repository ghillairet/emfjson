/*
 * Copyright (c) 2015 Guillaume Hillairet.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Guillaume Hillairet - initial API and implementation
 *
 */
package org.emfjson.jackson.tests.generics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emfjson.jackson.junit.generics.*;
import org.emfjson.jackson.support.StandardFixture;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.emfjson.jackson.databind.EMFContext.Attributes.RESOURCE_SET;
import static org.emfjson.jackson.databind.EMFContext.Attributes.ROOT_ELEMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GenericTest {

	@ClassRule
	public static StandardFixture fixture = new StandardFixture();

	private ObjectMapper mapper = fixture.mapper();
	private ResourceSet resourceSet = fixture.getResourceSet();

	@Test
	public void testSaveTwoObjectsWithTypeInformation() throws IOException {
		JsonNode expected = mapper.createObjectNode()
				.put("eClass", "http://www.emfjson.org/jackson/generics#//GenericContainer")
				.set("values", mapper.createArrayNode()
						.add(mapper.createObjectNode()
								.put("eClass", "http://www.emfjson.org/jackson/generics#//SpecialTypeOne")
								.put("value", "String"))
						.add(mapper.createObjectNode()
								.put("eClass", "http://www.emfjson.org/jackson/generics#//SpecialTypeTwo")
								.put("value", true)));

		Resource resource = resourceSet.createResource(URI.createURI("types-generic.json"));

		GenericContainer gc = GenericsFactory.eINSTANCE.createGenericContainer();
		SpecialTypeOne one = GenericsFactory.eINSTANCE.createSpecialTypeOne();
		one.setValue("String");
		SpecialTypeTwo two = GenericsFactory.eINSTANCE.createSpecialTypeTwo();
		two.setValue(true);
		gc.getValues().add(one);
		gc.getValues().add(two);
		resource.getContents().add(gc);

		assertEquals(expected, mapper.valueToTree(resource));
	}

	@Test
	public void testLoadTwoObjectsWithTypeInformation() throws IOException {
		Resource resource = resourceSet.createResource(
				URI.createURI("src/test/resources/tests/test-load-types-generic.json"));

		Map<Object, Object> options = new HashMap<>();
		// TODO
		options.put(ROOT_ELEMENT, GenericsPackage.eINSTANCE.getGenericContainer());
		resource.load(options);

		assertEquals(1, resource.getContents().size());

		EObject root = resource.getContents().get(0);
		assertTrue(root instanceof GenericContainer);

		GenericContainer container = (GenericContainer) root;

		assertEquals(2, container.getValues().size());
		GenericType<?> first = container.getValues().get(0);
		GenericType<?> second = container.getValues().get(1);

		assertTrue(first instanceof SpecialTypeOne);
		assertTrue(second instanceof SpecialTypeTwo);

		assertEquals("String", ((SpecialTypeOne) first).getValue());
		assertEquals(true, ((SpecialTypeTwo) second).getValue());
	}

	@Test
	public void testSaveObjectGeneric() {
		JsonNode expected = mapper.createObjectNode()
				.put("eClass", "http://www.emfjson.org/jackson/generics#//BaseOne")
				.set("containsOne", mapper.createObjectNode()
						.put("eClass", "http://www.emfjson.org/jackson/generics#//Any"));

		BaseOne b = GenericsFactory.eINSTANCE.createBaseOne();
		Any a = GenericsFactory.eINSTANCE.createAny();
		b.setContainsOne(a);

		assertThat((JsonNode) mapper.valueToTree(b))
				.isEqualTo(expected);
	}

	@Test
	public void testLoadObjectGeneric() throws JsonProcessingException {
		JsonNode data = mapper.createObjectNode()
				.put("eClass", "http://www.emfjson.org/jackson/generics#//BaseOne")
				.set("containsOne", mapper.createObjectNode()
						.put("eClass", "http://www.emfjson.org/jackson/generics#//Any"));

		EObject object = mapper
				.reader()
				.withAttribute(RESOURCE_SET, resourceSet)
				.treeToValue(data, EObject.class);

		assertThat(object)
				.isInstanceOf(BaseOne.class);

		BaseOne b = (BaseOne) object;
		assertThat(b.getContainsOne())
				.isNotNull();
	}
}
