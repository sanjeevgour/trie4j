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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.trie4j.util.Pair;

public abstract class AbstractTermIdMapTrie<T>
implements Externalizable, MapTrie<T>{
	protected AbstractTermIdMapTrie() {
	}

	protected AbstractTermIdMapTrie(TermIdTrie trie) {
		this.trie = trie;
	}

	@Override
	public boolean contains(String word) {
		return trie.contains(word);
	}

	@Override
	public Iterable<String> commonPrefixSearch(String query) {
		return trie.commonPrefixSearch(query);
	}

	@Override
	public int findWord(CharSequence chars, int start, int end,
			StringBuilder word) {
		return trie.findWord(chars, start, end, word);
	}

	@Override
	public Iterable<String> predictiveSearch(String prefix) {
		return trie.predictiveSearch(prefix);
	}

	@Override
	public void insert(String word) {
		trie.insert(word);
	}

	@Override
	public int size() {
		return trie.size();
	}

	@Override
	public void trimToSize() {
		trie.trimToSize();
	}

	@Override
	public void dump(Writer writer) throws IOException {
		trie.dump(writer);
	}

	@Override
	public void freeze() {
		trie.freeze();
	}

	public class MapNodeAdapter implements MapNode<T>{
		public MapNodeAdapter(TermIdNode orig){
			this.orig = orig;
		}

		@Override
		public char[] getLetters() {
			return orig.getLetters();
		}

		@Override
		public boolean isTerminate() {
			return orig.isTerminate();
		}

		@Override
		public MapNode<T> getChild(char c) {
			return new MapNodeAdapter(orig.getChild(c));
		}

		@Override
		@SuppressWarnings("unchecked")
		public MapNode<T>[] getChildren() {
			TermIdNode[] origArray = orig.getChildren();
			MapNode<T>[] ret = new MapNode[origArray.length];
			for(int i = 0; i < ret.length; i++){
				ret[i] = new MapNodeAdapter(origArray[i]);
			}
			return ret;
		}

		@Override
		@SuppressWarnings("unchecked")
		public T getValue() {
			return (T)values[orig.getTermId()];
		}

		@Override
		public void setValue(T value) {
			values[orig.getTermId()] = value;
		}

		private TermIdNode orig;
	}
	@Override
	public MapNode<T> getRoot() {
		return new MapNodeAdapter(trie.getRoot());
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(String text) {
		int id = trie.getTermId(text);
		if(id < 0) return null;
		return (T)values[id];
	}

	@Override
	public T insert(String word, T value) {
		throw new UnsupportedOperationException();
	}

	class IterableAdapter implements Iterable<Map.Entry<String, T>>{
		public IterableAdapter(Iterable<Pair<String, Integer>> iterable) {
			this.iterable = iterable;
		}
		@Override
		public Iterator<Map.Entry<String, T>> iterator(){
			final Iterator<Pair<String, Integer>> it = iterable.iterator();
			return new Iterator<Map.Entry<String, T>>(){
				@Override
				public boolean hasNext() {
					return it.hasNext();
				}
				@Override
				public Map.Entry<String, T> next() {
					final Pair<String, Integer> e = it.next();
					return new Map.Entry<String, T>() {
						@Override
						public String getKey() {
							return e.getFirst();
						}
						@Override
						@SuppressWarnings("unchecked")
						public T getValue() {
							return (T)values[e.getSecond()];
						}
						@Override
						public T setValue(T value) {
							T ret = getValue();
							values[e.getSecond()] = value;
							return ret;
						}
					};
				}
				@Override
				public void remove() {
					it.remove();
				}
			};
		}
		private Iterable<Pair<String, Integer>> iterable;
	}
	@Override
	public Iterable<Map.Entry<String, T>> commonPrefixSearchEntries(final String query) {
		return new IterableAdapter(trie.commonPrefixSearchWithTermId(query));
	}

	@Override
	public Iterable<Entry<String, T>> predictiveSearchEntries(String prefix) {
		return new IterableAdapter(trie.predictiveSearchWithTermId(prefix));
	}

	@Override
	public void readExternal(ObjectInput in)
	throws IOException, ClassNotFoundException {
		trie = (TermIdTrie)in.readObject();
		values = (Object[])in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(trie);
		out.writeObject(values);
	}

	public TermIdTrie getTrie() {
		return trie;
	}

	public void setTrie(TermIdTrie trie) {
		this.trie = trie;
	}

	protected Object[] getValues(){
		return values;
	}

	protected void setValues(Object[] values){
		this.values = values;
	}

	protected Object[] values = {};
	private TermIdTrie trie;
}