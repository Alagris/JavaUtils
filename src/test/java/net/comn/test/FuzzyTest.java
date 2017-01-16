package net.comn.test;

import org.junit.Assert;
import org.junit.Test;

import net.comn.src.text.FuzzyComparator;


public class FuzzyTest {
	
	FuzzyComparator comparator = new FuzzyComparator();

	@Test
	public void test1(){
		final String a="hello";
		final String b="hilloo";
		Assert.assertTrue(comparator.simpleCompare(a,b)==comparator.simpleCompare(b,a));
	}
	
	@Test
	public void test2(){
		compare("adam","ada");
		compare("mozart","MOZART");
		compare("really","rails");
		compare("really","rice");
		compare("really","realistic");
		compare("really","reallity");
		compare("really","really");
	}
	
	@Test
	public void test3(){
		compare("Freundschaftsbezeugung","Freundschaftsbezeugung");
		compare("Freundschaftsbezeugung","Freund");//beginning
		compare("Freundschaftsbezeugung","fReUnDsHaFtSbEzeUgUnG");//different cases
		compare("Freundschaftsbezeugung","bezeugung");//ending
		compare("Freundschaftsbezeugung","etwas");//something completely different and short
		compare("Freundschaftsbezeugung","EtwasGanzAnderesUndLanges");//something completely different and long
		
	}
	
	private void compare(final String a,final String b){
		System.out.println("cmp("+a+","+b+")="+ comparator.simpleCompare(a,b));
	}
	
	
}
