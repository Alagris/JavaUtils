package net.comn.src.text;

import java.util.Arrays;

public class QuickString implements CharSequence
{
	private char[] chars;

	public QuickString(int size)
	{
		chars = new char[size];
	}
	public QuickString(char [] chars)
	{
		this.chars=chars;
	}
	public QuickString(char [] chars,int startInclusive,int endExclusive)
	{
		if (startInclusive < 0) { throw new StringIndexOutOfBoundsException(startInclusive); }
		if (endExclusive > chars.length) { throw new StringIndexOutOfBoundsException(endExclusive); }
		int subLen = endExclusive - startInclusive;
		if (subLen < 0) { throw new StringIndexOutOfBoundsException(subLen); }
		this.chars=new char[subLen];
		for(int i=0,j=startInclusive;j<endExclusive;i++,j++){
			this.chars[i]=chars[j];
		}
	}
	@Override
	public int length()
	{
		return chars.length;
	}

	@Override
	public char charAt(int index)
	{
		return chars[index];
	}
	
	public void setAt(int index,char c)
	{
		chars[index]=c;
	}
	/** Start inclusive, end exclusive */
	@Override
	public QuickString subSequence(int start, int end)
	{
		return ((start == 0) && (end == length())) ? this : new QuickString(getChars(), start, end);
	}

	public char[] getChars()
	{
		return chars;
	}

	public void setChars(char[] chars)
	{
		this.chars = chars;
	}
	
	public static QuickString copy(QuickString s){
		return new QuickString(Arrays.copyOf(s.getChars(), s.length()));
	}
	
	public static QuickString condensateStrings(CharSequence[] strings){
		int totalSize=0;
		for(int i=0;i<strings.length;i++){
			totalSize+=strings[i].length();
		}
		QuickString quickString = new QuickString(totalSize);
		for(int i=0,k=0;i<strings.length;i++){
			CharSequence seq=strings[i];
			for(int j=0;j<seq.length();j++,k++){
				quickString.setAt(k,seq.charAt(j));
			}
		}
		return quickString;
	}
	
	@Override
	public String toString()
	{
		return new String(getChars());
	}
}
