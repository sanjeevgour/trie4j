/*
 * Copyright 2014 Takao Nakaguchi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trie4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;
import org.trie4j.patricia.simple.MapPatriciaTrie;
import org.trie4j.test.WikipediaTitles;

public abstract class AbstractMapTrieWikipediaSerializeTest
extends AbstractWikipediaSerializeTest{
	protected MapTrie<Integer> newTrie(){
		return new MapPatriciaTrie<Integer>();
	}

	protected MapTrie<Integer> buildSecondTrie(MapTrie<Integer> firstTrie){
		return firstTrie;
	}


	@Test
	public void test() throws Exception{
		WikipediaTitles wt = new WikipediaTitles();
		MapTrie<Integer> trie = wt.insertTo(newTrie());
		trie = buildSecondTrie(trie);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(trie);
		oos.flush();
		byte[] serialized = baos.toByteArray();
		System.out.println("size: " + serialized.length);
		Trie t = (Trie)new ObjectInputStream(new ByteArrayInputStream(serialized))
				.readObject();
		wt.assertAllContains(t);
	}
}