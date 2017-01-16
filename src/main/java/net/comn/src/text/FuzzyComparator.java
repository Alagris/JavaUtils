package net.comn.src.text;

import java.nio.charset.StandardCharsets;

public class FuzzyComparator {

	private static final boolean DEBUG = false;
	static {
		if (DEBUG)
			System.out.println("DEBUG MODE ON!");
	}

	//////////////////////////////////////////
	//// similarity rate:
	//// then less the better
	//// minimum 0
	//// maximum - who knows?
	//// comparing A to B gives (roughly) the same result as B to A
	//////////////////////////////////////////

	private class SearchingPosition {
		int currentIndexInArray1 = 0;
		int currentIndexInArray2 = 0;
		/**
		 * score is added each time a correlation is found. The added value is
		 * equal:<br>
		 * how-far-from-expected-place-in-array-2-has-a-correlating-byte-from-array-1-been-found
		 * MULTIPLIED BY (
		 * how-many-bytes-from-array-1-have-been-skipped-in-order-to-reach-the-correlating-byte-from-array-1
		 * + 1)<br>
		 * the there was a difference
		 */
		float score = 0;
		float scoreToAdd = 0;
		/**
		 * Each correlations signifies that a pair of the same (or different
		 * case) bytes (one from array 1 and one from array 2) has been found.
		 */
		int countOfRegisteredByteCorrelations = 0;

		void foundByteCorrelation() {
			countOfRegisteredByteCorrelations++;
		}

		int countOfSkippedBytesInArray(final int arrayLength) {
			return arrayLength - countOfRegisteredByteCorrelations;
		}

		void increase() {
			currentIndexInArray1++;
			currentIndexInArray2++;
		}
	}

	public int fuzzify(final float rawSimilarityRate){
		return fuzzify(rawSimilarityRate,1);
	}
	/**@param fuzzificationGradient - should be in range of 0 (exclusive) and 1 (inclusive).
	 * Values higher than 1 don't really fuzzify at all but make the result sharper instead.
	 * <br>
	 * Notice the optimal fuzzificationGradient depends on average length of compared strings/byte arrays*/
	public int fuzzify(final float rawSimilarityRate,final float fuzzificationGradient){
		//very simple even though sounds
		//like high class math
		return (int)(rawSimilarityRate*fuzzificationGradient+0.5f);
	}
	/**
	 * We always attempt to find smaller array inside bigger one. Returned value
	 * is the raw similarity rate. You may want to add extra fuzzification by
	 * applying<br>
	 * <code>fuzzify</code>
	 */
	public float simpleCompare(final String s1, final String s2) {
		if (s1.length() > s2.length()) {
			return simpleCompareArray1To2(s1.getBytes(StandardCharsets.UTF_8), s2.getBytes(StandardCharsets.UTF_8));
		} else {
			return simpleCompareArray1To2(s2.getBytes(StandardCharsets.UTF_8), s1.getBytes(StandardCharsets.UTF_8));
		}
	}
	/**
	 * We always attempt to find smaller array inside bigger one. Returned value
	 * is the raw similarity rate. You may want to add extra fuzzification by
	 * applying<br>
	 * <code>fuzzify</code>
	 */
	public float simpleCompare(final byte[] s1, final byte[] s2) {
		if (s1.length > s2.length) {
			return simpleCompareArray1To2(s1, s2);
		} else {
			return simpleCompareArray1To2(s2, s1);
		}
	}

	/**
	 * In case the algorithms loses track of array 1 it will just skip a few
	 * bytes and start all over again.This is especially useful for cases like:
	 * array1="hello world" <br>
	 * array2="goodbye world" <br>
	 * Algorithm would lose track of array 1 because it could not locate the
	 * beginning, however the ending is completely fine.
	 */
	private float simpleCompareArray1To2(final byte[] array1, final byte[] array2) {
		if (DEBUG) {
			System.out.println("SimpleCompare(array1=" + new String(array1) + " , array2=" + new String(array2) + ")");
		}
		final SearchingPosition currentPosition = new SearchingPosition();
		while (currentPosition.currentIndexInArray1 < array1.length) {
			currentPosition.currentIndexInArray2 = scanArray2SearchingForBytesFromArray1(array1, array2,
					currentPosition);
			currentPosition.currentIndexInArray1 += MAX_SKIPPED_BYTES_FROM_ARRAY_1;

		}
		final float PENALTY_FOR_SKIPPING_BYTES_IN_ARRAY_1 = 1.5f * MAX_SKIPPED_BYTES_FROM_ARRAY_1 / 4;// rule
																										// of
																										// thumb
		final float PENALTY_FOR_SKIPPING_BYTES_IN_ARRAY_2 = 1f * RANGE_OF_SEARCH_IN_ARRAY_2 / 4;// rule
																								// of
																								// thumb
		if (DEBUG) {
			System.out.println("Skipped in array1= " + currentPosition.countOfSkippedBytesInArray(array1.length));
			System.out.println("Skipped in array2= " + currentPosition.countOfSkippedBytesInArray(array2.length));
		}
		currentPosition.score += currentPosition.countOfSkippedBytesInArray(array1.length)
				* PENALTY_FOR_SKIPPING_BYTES_IN_ARRAY_1;
		currentPosition.score += currentPosition.countOfSkippedBytesInArray(array2.length)
				* PENALTY_FOR_SKIPPING_BYTES_IN_ARRAY_2;
		return currentPosition.score;
	}

	private static final int RANGE_OF_SEARCH_IN_ARRAY_2 = 4;
	private static final int MAX_SKIPPED_BYTES_FROM_ARRAY_1 = 4;

	/**
	 * This is the actual core logic of algorithm: <br>
	 * array1="abcd" <br>
	 * array2="ahjcd"<br>
	 * 1. search for a in range of 4 next bytes/chars:
	 * 
	 * <pre>
	 * - a=a - true
	 * </pre>
	 * 
	 * 2. search for b in range of 4 next bytes/chars:
	 * 
	 * <pre>
	 * - h=b - false
	 * - j=b - false
	 * - c=b - false
	 * - d=b - false
	 * skipping b
	 * - h=c - false
	 * - j=c - false
	 * - c=c - true
	 * </pre>
	 * 
	 * 3. searching for
	 */
	private int scanArray2SearchingForBytesFromArray1(final byte[] array1, final byte[] array2,
			final SearchingPosition currentPosition) {
		int oneIndexAfterTheLastFoundByteFromArray2ThatCorrespondsToSomeByteFromArray1 = currentPosition.currentIndexInArray2;
		while (currentPosition.currentIndexInArray2 < array2.length
				&& currentPosition.currentIndexInArray1 < array1.length) {
			if (tryToFindAnyByteFromArray1In2(array1, array2, currentPosition)) {
				currentPosition.increase();
				oneIndexAfterTheLastFoundByteFromArray2ThatCorrespondsToSomeByteFromArray1 = currentPosition.currentIndexInArray2;
			} else {
				currentPosition.currentIndexInArray2 += RANGE_OF_SEARCH_IN_ARRAY_2;
			}
		}
		return oneIndexAfterTheLastFoundByteFromArray2ThatCorrespondsToSomeByteFromArray1;
	}

	/**
	 * modifies SearchingPosition and returns true. If It couldn't find any
	 * corresponding byte from array 1 in array 2 then the currentPosition is
	 * left unmodified and false is returned
	 */
	private boolean tryToFindAnyByteFromArray1In2(final byte[] array1, final byte[] array2,
			final SearchingPosition currentPosition) {

		for (int skippedBytesInArray1 = 0, positionInArray1 = currentPosition.currentIndexInArray1; skippedBytesInArray1 < MAX_SKIPPED_BYTES_FROM_ARRAY_1
				&& positionInArray1 < array1.length; skippedBytesInArray1++, positionInArray1++) {

			final int indexOfByteFromArray2CorrespondingToByteInArray1 = tryToFindByteFromArray1In2(array1, array2,
					currentPosition.currentIndexInArray2, positionInArray1, currentPosition);

			if (indexOfByteFromArray2CorrespondingToByteInArray1 > -1) {
				currentPosition.foundByteCorrelation();
				currentPosition.score += currentPosition.scoreToAdd * (skippedBytesInArray1 + 1);
				currentPosition.scoreToAdd = 0;
				currentPosition.currentIndexInArray2 = indexOfByteFromArray2CorrespondingToByteInArray1;
				currentPosition.currentIndexInArray1 = positionInArray1;
				return true;
			}
		}
		return false;
	}

	private int tryToFindByteFromArray1In2(final byte[] array1, final byte[] array2, final int fromInclusive2,
			final int byteAt1, final SearchingPosition scoreHandle) {
		return getIndexOf(array2, array1[byteAt1], fromInclusive2, RANGE_OF_SEARCH_IN_ARRAY_2, scoreHandle);
	}

	private int getIndexOf(final byte[] array, final byte b, final int fromInclusive, final int length,
			final SearchingPosition scoreHandle) {
		final int limit = Math.min(length, array.length - fromInclusive);

		for (int i = 0; i < limit; i++) {
			/**
			 * This is awesome way to understand the algorithm. Just run and see
			 * how it works
			 */
			if (DEBUG)
				System.out.print("index in array2 = " + fromInclusive + " + " + i + " = " + (i + fromInclusive)
						+ "! Searching for byte from array 1 = " + (char) b + "! Compared to array 2 = "
						+ ((char) array[fromInclusive + i]));
			if (array[fromInclusive + i] == b) {
				scoreHandle.scoreToAdd += i;
				if (DEBUG)
					System.out.println("<FOUND");
				return fromInclusive + i;
			} else if (Character.toLowerCase(array[fromInclusive + i]) == Character.toLowerCase(b)) {
				scoreHandle.scoreToAdd += i + 0.5f;
				if (DEBUG)
					System.out.println("<FOUND different case");
				return fromInclusive + i;
			}
			if (DEBUG)
				System.out.println();
		}

		return -1;
	}

	// Basically the compact (and slightly simplified) version of all those
	// craps above... only
	// less readable:
	//
	// /**
	// * Returned value is always bigger or equal 0 Very fuzzy. Could skip some
	// * letters in mainWord if distance is too far
	// */
	// public int fuzzyCompare(final String w1, final String w2) {
	// int output = 0;
	// // scan w1 to find chars in w2
	// // when w2.size() <= w1.size()
	// // for every part in w1
	// // try to find next char from w2
	// // a - index of chars from w2 that is being searched for in current part
	// // b - index of currently scanned char in w1
	// // c - index of the last found char in w2
	// // s - part beginning (including)
	// // e - part end (excluding)
	// // iterating through parts
	// final int searchRangeExcluding = 4;
	// for (int s = 0, e = searchRangeExcluding, c = 0; c < w2.length() && s <
	// e; c++) {
	// // iterating through chars in w2
	// int b = 0;
	// second: for (int a = c; a < w2.length(); a++) {
	// // iterating through current part in w1
	// b = s;
	// for (; b < e; b++) {
	// if (w1.charAt(b) == w2.charAt(a)) {
	// output += searchRangeExcluding - (b - s);
	// c = a;
	// break second;
	// } else if (Character.isLowerCase(w1.charAt(b)) ==
	// Character.isLowerCase(w2.charAt(a))) {
	// output += Math.max(searchRangeExcluding - (b - s) - 1, 0);
	// c = a;
	// break second;
	// }
	// }
	// }
	// s = b + 1;
	// e = Math.min(s + searchRangeExcluding, w1.length());
	//
	// }
	// return (w1.length() == w2.length()) ? output : (output + 1);
	// }

}
