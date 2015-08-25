/** Ben F Rayfield offers Xob (Xorlisp Objects) opensource GNU LGPL */
package humanainet.xob.ptr32;
import humanainet.acyc.Glo;
import humanainet.acyc.ptr32.CacheAcyc32;
import humanainet.xob.XobUtil;

/** TODO use parts of visualIntFactor software here? */
public class NumberUtil{
	
	/** 2 untyped varSizeScalar of the same size, wrapped in this type. (real complex).
	Its type is the UTF16 bits of "varSizeComplex", using a typVal as usual.
	Its value is a powerOf2Item whose objects are all bit0 or bit1. It can be any
	size thats a power of 2 number of bits for each of the fractional part and
	integer part of each of the real and imaginary parts. The first branch is
	(real imaginary). Each of those is a twosComplement scalar
	(integerBits fractionBits). integerBits and fractionBits must both
	be the same depth (of complete binary tree with bits at its object leafs)
	but can be any powerOf2. This can practically exceed float64 range since
	the exponent can double or half (squaring or squareRooting the scalar value)
	with each 1 level deeper because it doubles the number of digits, but we
	only have to pay for the digits we use sometimes plus logBase2 more
	lispPairs since we can reuse large blocks of bit0 which are themself
	made of half size block of bit0 recursively. This number type can perform
	the functions of bigDecimal and bigComplex while also being memory
	efficient for small numbers like 1/2 or 3/8 or small complexNums.
	This number type has only 1 value that means 0 since it uses twosComplement
	in each of real and imaginary parts. You only pay for the parts you use,
	so if you always use reals, just reuse a big block of 0s (which reuses half
	size block of 0s recursively) for the complex part. Practically this will
	only take a few times more space than common number types, but its far less
	efficient for computing with them (like in my opensource software visualIntFactor)
	because its more of a data format.
	*/
	public static final int typeVarSizeComplex;
	
	/** twosComplement fixedPoint (instead of floatingPoint) scalar.
	Complete binary tree of bits. Depth of that tree can be anything.
	Number of bits doubles with each increase of depth by 1.
	Efficiently supports floating points with very large or small exponents by using
	the sparseness of acyc. For example, you can reuse a large block of bit0
	which reuses the same half size block of bit0 twice, and so on.
	See comment in typeVarSizeComplex which is 2 of untyped of these the same size.
	*/
	public static final int typeVarSizeScalar;

	/** An efficient kind of number because all its values are preallocated in CacheAcyc32
	or (TODO) are emulated without storing that data as a certain range that size in the addresses.
	This is a powerOf2Item of bits (a complete binary tree of depth 16).
	*/
	public static final int typeNonnegInt16;
	
	static{
		typeVarSizeComplex = XobUtil.stringToListOfPowerOf2(Glo.cacheacyc, "varSizeComplex");
		Glo.econacyc.oneMoreRefFromOutside(typeVarSizeComplex);
		typeVarSizeScalar = XobUtil.stringToListOfPowerOf2(Glo.cacheacyc, "varSizeScalar");
		Glo.econacyc.oneMoreRefFromOutside(typeVarSizeScalar);
		typeNonnegInt16 = XobUtil.stringToListOfPowerOf2(Glo.cacheacyc, "nonnegInt16");
		Glo.econacyc.oneMoreRefFromOutside(typeNonnegInt16);
	}

}
