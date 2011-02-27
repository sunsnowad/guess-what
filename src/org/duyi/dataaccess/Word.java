/**
 * 
 */
package org.duyi.dataaccess;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Yi Du
 * 
 */
public class Word {
	private int wordid;
	private String word;
	private ArrayList<String> descriptions;
	private ArrayList<String> usedDesc;
	private String guessedWord;// word guessed by user
	private int startRand;

	public Word(int cursorWord) {
		this.wordid = cursorWord;
		descriptions = new ArrayList<String>();
		usedDesc = new ArrayList<String>();
		startRand = -1;
	}

	public int getWordid() {
		return wordid;
	}

	public void setWordid(int wordid) {
		this.wordid = wordid;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public ArrayList<String> getDescriptions() {
		return descriptions;
	}

	public String getDescriptionByIndex(int index) {
		if (descriptions.size() == 0)
			return null;
		if (index >= descriptions.size())
			return null;
		if (startRand == -1) {
			startRand = new Random(System.currentTimeMillis())
					.nextInt(descriptions.size());
		}
		index = (index + startRand) % (descriptions.size());
		usedDesc.add(descriptions.get(index));
		return descriptions.get(index);
	}

	public void addDescriptions(String description) {
		descriptions.add(description);
	}

	public String getGuessedWord() {
		return guessedWord;
	}

	public void setGuessedWord(String guessedWord) {
		this.guessedWord = guessedWord;
	}

	public ArrayList<String> getUsedDesc() {
		return usedDesc;
	}

}
