.include "./cs47_proj_macro.asm"
.text
.globl au_normal
#####################################################################
# Implement au_normal
# Argument:
# 	$a0: First number
#	$a1: Second number
#	$a2: operation code ('+':add, '-':sub, '*':mul, '/':div)
# Return:
#	$v0: ($a0+$a1) | ($a0-$a1) | ($a0*$a1):LO | ($a0 / $a1)
# 	$v1: ($a0 * $a1):HI | ($a0 % $a1)
# Notes:
#####################################################################
au_normal:
	addi	$sp $sp -24
	sw	$fp 24($sp)
	sw	$ra 20($sp)
	sw	$a0 16($sp) # First arg
	sw	$a1 12($sp) # Second arg
	sw	$a2 8($sp) # Arith type
	addi	$fp $sp 24
	
	beq $a2 '+' funcsum_norm
	beq $a2 '-' funcdiff_norm
	beq $a2 '*' funcprod_norm
	beq $a2 '/' funcdiv_norm
	
	# Arithmetic using normal MIPS operations
	
	funcsum_norm:
	add 	$v0 $a0 $a1
	j	exitproc_norm
	
	funcdiff_norm:
	sub	$v0 $a0 $a1
	j	exitproc_norm
	
	funcprod_norm:
	mult	$a0 $a1
	mfhi	$v1
	mflo	$v0
	j	exitproc_norm
	
	funcdiv_norm:
	div	$a0 $a1
	mfhi	$v1
	mflo	$v0
	j	exitproc_norm
	
	exitproc_norm:
	lw	$a2 -16($fp)
	lw	$a1 -12($fp)
	lw	$a0 -8($fp)
	lw 	$ra -4($fp)
	lw	$fp 0($fp)
	addi	$sp $sp 20
	
	# Return
	jr	$ra