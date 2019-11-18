# Add you macro definition here - do not touch cs47_common_macro.asm"
#<------------------ MACRO DEFINITIONS ---------------------->#

# Macro Extract Nth Bit
.macro retrieve_nth_bit($regDest, $regSrc, $regTarg)
	srlv	$regSrc $regSrc $regTarg	# Shifts $regSrc by the target amount and stores it back into $regSrc
	li	$regDest 1			# Sets $regDest to 1
	and	$regDest $regSrc $regDest	
.end_macro

# Macro Retrieve First Bit
.macro retrieve_first_bit($regDest, $regSrc)
	li	$regDest 1
	and	$regDest $regSrc $regDest
	srl	$regSrc $regSrc 1
.end_macro

# Macro Insert To Nth Bit
.macro put_one_to_nth_bit($regDest, $regSrc, $regTarg, $maskReg)
	move 	$maskReg $regTarg		# Moves value in $regT to $maskReg
	sllv 	$maskReg $maskReg $regSrc	# Shifts $maskReg by $regSrc
	or	$regDest $regDest $maskReg	
.end_macro

# Half-Add Util
.macro half_add_util($arg1, $arg2, $sum, $car)
	xor	$sum $arg1 $arg2
	and	$car $arg1 $arg2
.end_macro

# Full-Add Util
.macro full_add_util($arg1, $arg2, $sum, $prevcar, $carin, $carout)
	half_add_util($arg1 $arg2 $sum $prevcar)
	and	$carout $carin $sum
	xor	$sum $carin $sum
	xor	$carout $carout $prevcar
.end_macro