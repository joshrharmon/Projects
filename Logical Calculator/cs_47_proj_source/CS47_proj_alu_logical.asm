.include "./cs47_proj_macro.asm"
.text
.globl au_logical
# TBD: Complete your project procedures
# Needed skeleton is given
#####################################################################
# Implement au_logical
# Argument:
# 	$a0: First number
#	$a1: Second number
#	$a2: operation code ('+':add, '-':sub, '*':mul, '/':div)
# Return:
#	$v0: ($a0+$a1) | ($a0-$a1) | ($a0*$a1):LO | ($a0 / $a1)
# 	$v1: ($a0 * $a1):HI | ($a0 % $a1)
# Notes:
#####################################################################
au_logical:
	addi	$sp $sp -48
	sw	$fp 48($sp)
	sw	$ra 44($sp)
	sw	$s0 40($sp)
	sw	$s1 36($sp)
	sw	$s2 32($sp)
	sw	$s3 28($sp)
	sw	$s4 24($sp)
	sw	$s5 20($sp)
	sw	$a0 16($sp)
	sw	$a1 12($sp)
	sw	$a2 8($sp)
	addi	$fp $sp 48

	
	# Determine arithmetic operation and jump to it
	beq	$a2 '+' sum_logi
	beq	$a2 '-' diff_logi
	beq	$a2 '*' prod_logi
	beq	$a2 '/' div_logi
	j	func_exit
	
	# Each of the arithmetic logic operations
	sum_logi:
	jal	funcsum_logi
	j	func_exit
	
	diff_logi:
	jal	funcdiff_logi
	j	func_exit
	
	prod_logi:
	jal	funcprod_logi_signed
	j	func_exit
	
	div_logi:
	jal	funcdiv_logi_signed
	j	func_exit
	
	func_exit:
	lw	$fp 48($sp)
	lw	$ra 44($sp)
	lw	$s0 40($sp)
	lw	$s1 36($sp)
	lw	$s2 32($sp)
	lw	$s3 28($sp)
	lw	$s4 24($sp)
	lw	$s5 20($sp)
	lw	$a0 16($sp)
	lw	$a1 12($sp)
	lw	$a2 8($sp)
	addi	$sp $sp 48

	jr	$ra

#////////////////////////////////////////////////
# Procedure: funcsum_logi			/
# $a0: 1st argument				/
# $a1: 2nd argument				/	
# Purpose: Output $a0 + $a1 into $v0		/
#////////////////////////////////////////////////
funcsum_logi:
	# Initialize the stack
	addi	$sp $sp -48
	sw	$fp 48($sp)
	sw	$ra 44($sp)
	sw	$s0 40($sp) # Final Sum
	sw	$s1 36($sp) # First Arg
	sw	$s2 32($sp) # Second Arg
	sw	$s3 28($sp) # Temp Sum
	sw	$s4 24($sp) # Carry
	sw	$s5 20($sp) 
	sw	$a0 16($sp)
	sw	$a1 12($sp)
	sw	$a2 8($sp)
	addi	$fp $sp 48
	
	li	$s0 0 # Final Answer
	li	$t0 0 # Index counter
	li	$t3 0 # Carry-in
	li	$t4 0 # Carry-out
	
	# $t3 = Carry out
	# $t4 = Carry in
	
	funcsum_loop:
	beq	$t0 32 funcsum_loop_end
	
	retrieve_first_bit($s1 $a0)
	retrieve_first_bit($s2 $a1)
	
	full_add_util($s1 $s2 $s3 $s4 $t4 $t3)
	put_one_to_nth_bit($s0 $t0 $s3 $t4)
	
	move	$t4 $t3		# Transfer carry
	addi	$t0 $t0 1	# Increment counter
	j	funcsum_loop
	
	funcsum_loop_end:
	move	$v0 $s0		# Transfer final sum to result
	move	$v1 $t3		# Transfer final overflow to result
	
	lw	$fp 48($sp)
	lw	$ra 44($sp)
	lw	$s0 40($sp)
	lw	$s1 36($sp)
	lw	$s2 32($sp)
	lw	$s3 28($sp)
	lw	$s4 24($sp)
	lw	$s5 20($sp)
	lw	$a0 16($sp)
	lw	$a1 12($sp)
	lw	$a2 8($sp)
	add	$sp $sp 48
	
	jr	$ra
		
#////////////////////////////////////////////////
# Procedure: funcdiff_logi			/
# $a0: 1st argument				/
# $a1: 2nd argument				/	
# Purpose: Output $a0 - $a1 into $v0		/
#////////////////////////////////////////////////

funcdiff_logi:
	# Initialize the stack
	addi $sp $sp -20
	sw	$a0 20($sp)
	sw	$a1 16($sp)
	sw	$ra 12($sp)
	sw	$fp 8($sp)
	addi	$fp $sp 20	
	
	neg	$a1 $a1		# Invert $a1
	jal	funcsum_logi	# Utilize add function
	
	lw	$a0 20($sp)
	lw	$a1 16($sp)
	lw	$ra 12($sp)
	lw	$fp 8($sp)
	addi	$sp $sp 20
	
	jr	$ra
	
#////////////////////////////////////////////////
# Procedure: funcprod_logi_unsigned		/
# $a0: 1st argument (multiplicand)		/
# $a1: 2nd argument (multiplier)		/	
# Purpose: Output LO to $v0, HI to $v1		/
#////////////////////////////////////////////////	
funcprod_logi_unsigned:
	addi	$sp $sp -56
	sw	$fp 56($sp)
	sw	$ra 52($sp)
	sw	$s0 48($sp) # Temp store for multiplier
	sw	$s1 44($sp) # Counter
	sw	$s2 40($sp) # Store multiplicand
	sw	$s3 36($sp) # 1 sll to 31
	sw	$s4 32($sp) 
	sw	$s5 28($sp) # Ending product LO
	sw	$s6 24($sp) # Ending product HI
	sw	$s7 20($sp) 
	sw	$a0 16($sp)
	sw	$a1 12($sp)
	sw	$a2 8($sp)
	addi	$fp $sp 56
	
	move 	$s2 $a0 # Stores multiplicand
	move	$s0 $a1 # Stores multiplier
	
	# Initialize variables
	add	$s1 $zero $zero
	add	$s3 $zero 1
	sll	$s3 $s3 31
	add	$s5 $zero $zero
	add	$s6 $zero $zero
	
	funcprod_logi_unsigned_looper:
	beq	$s1 33 funcprod_logi_unsigned_done
	
	retrieve_first_bit($t0 $s0) # Get first bit of current multiplier
	srl $s5 $s5 1 # Shift LO register to make room for incoming HI register bit
	retrieve_first_bit($t1, $s6) # Get first bit of HI 
	beq $t1 1 add_to_LO
	continue_1:	
	beq $t0 1 add_to_HI
	continue_2:
	addi $s1 $s1 1 # Increment counter
	j	funcprod_logi_unsigned_looper
	
	add_to_LO:
	add $s5 $s5 $s3 # Adds 31 bit-shifted to left 1 value
	j	continue_1
	
	add_to_HI:
	add $s6 $s6 $s2 # Adds multiplicand to HI if LSB of multiplier is 1
	j	continue_2

	funcprod_logi_unsigned_done:
	move	$v0 $s5
	move	$v1 $s6
	
	lw	$fp 56($sp)
	lw	$ra 52($sp)
	lw	$s0 48($sp) 
	lw	$s1 44($sp) 
	lw	$s2 40($sp) 
	lw	$s3 36($sp) 
	lw	$s4 32($sp) 
	lw	$s5 28($sp) 
	lw	$s6 24($sp) 
	lw	$s7 20($sp) 
	lw	$a0 16($sp)
	lw	$a1 12($sp)
	lw	$a2 8($sp)
	addi	$sp $sp 56
	
	jr	$ra

#////////////////////////////////////////////////
# Procedure: funcprod_logi_signed		/
# $a0: 1st argument				/
# $a1: 2nd argument				/	
# Purpose: Output LO to $v0, HI to $v1		/
#////////////////////////////////////////////////
funcprod_logi_signed:
	addi	$sp $sp -56
	sw	$fp 56($sp)
	sw	$ra 52($sp)
	sw	$s0 48($sp) # Stores the multiplicand
	sw	$s1 44($sp) # Stores the multiplier
	sw	$s2 40($sp) # Stores 31 to find MSB
	sw	$s3 36($sp) # Holds MSB of multiplicand
	sw	$s4 32($sp) # Holds MSB of multiplier
	sw	$s5 28($sp) # Stores whether both are negative (1), or other (positive, 0) 
	sw	$s6 24($sp) # Stores LO of final product
	sw	$s7 20($sp) # Stores HI of final product
	sw	$a0 16($sp)
	sw	$a1 12($sp)
	sw	$a2 8($sp)
	addi	$fp $sp 56
	
	move	$s0 $a0 # Make copy of multiplicand
	move	$s1 $a1 # Make copy of multiplier
	move	$t0 $s0 # Make temp copy of mcand
	move	$t1 $s1 # Make temp copy mplier
	addi	$s2 $zero 31
	
	retrieve_nth_bit($s3 $t0 $s2) # Checks MSB of multiplicand to see if negative
	retrieve_nth_bit($s4 $t1 $s2) # Checks MSB of multiplier to see if negative
	
	jal	conv_2s_comp_neg # Does necessary conversion of current $a0 (Multiplicand)
	move	$s0 $v0 # Stores result in multiplicand
	move	$a0 $s1 # Stores multiplier into $a0 register for checking
	jal	conv_2s_comp_neg # # Does necessary conversion of current $a0 (Multiplier)
	move	$s1 $v0 # Stores result in copy register
	move	$a0 $s0 # Store new multiplicand into $a0
	move	$a1 $s1 # Store new multiplier into $a1
	
	xor	$s5 $s3 $s4 # XORs MSB of multiplicand and multiplier to see if both negative (1) or other (0)
	jal	funcprod_logi_unsigned # Jumps to unsigned mult logic
	
	move	$s6 $v0 # Initialize $s6 to the LO
	move	$s7 $v1 # Initialize $s7 to the HI
	
	beqz	$s5 funcprod_logi_signed_done
	move	$a0 $s6
	move	$a1 $s7
	jal	conv_2s_comp_sixfour
	move	$s6 $v0
	move	$s7 $v1
			
	funcprod_logi_signed_done:
	move	$v0 $s6
	move	$v1 $s7
	
	lw	$fp 56($sp)
	lw	$ra 52($sp)
	lw	$s0 48($sp) 
	lw	$s1 44($sp) 
	lw	$s2 40($sp) 
	lw	$s3 36($sp)
	lw	$s4 32($sp) 
	lw	$s5 28($sp) 
	lw	$s6 24($sp) 
	lw	$s7 20($sp) 
	lw	$a0 16($sp)
	lw	$a1 12($sp)
	lw	$a2 8($sp)
	addi	$sp $sp 56
	
	jr	$ra

#////////////////////////////////////////////////
# Procedure: funcdiv_logi_unsigned		/
# $a0: 1st argument (Dividend)			/	
# $a1: 2nd argument (Divisor)			/
# Purpose: Output $a1/$a0 to $v0 and remainder  /
# to $v1					/
#////////////////////////////////////////////////
funcdiv_logi_unsigned:
	addi	$sp $sp -56
	sw	$fp 56($sp)
	sw	$ra 52($sp)
	sw	$s0 48($sp) # Remainder (HI)
	sw	$s1 44($sp) # Quotient (LO)
	sw	$s2 40($sp) # Divisor
	sw	$s3 36($sp) # Counter
	sw	$s4 32($sp) # Stores temporary subtration between remainder-divisor
	sw	$s5 28($sp) # Stores 31 to check for MSB
	sw	$s6 24($sp) 
	sw	$s7 20($sp)
	sw	$a0 16($sp)
	sw	$a1 12($sp)
	sw	$a2 8($sp)
	addi	$fp $sp 56
	
	add	$s0 $zero $zero 	# Initialize remainder to 0
	add	$s1 $zero $zero 	# Initialize quotient to 0
	add	$s2 $zero $zero 	# Initialize divisor to 0
	add	$s3 $zero $zero		# Initialize counter to 0
	add	$s4 $zero $zero		# Initialize temporary storage variable to 0
	add	$s5 $zero 31
	
	move	$s1 $a0 		# Copies dividend to quotient register
	move	$s2 $a1			# Copies divisor to divisor register
	
	funcdiv_logi_unsigned_looper:
	beq	$s3 32 func_logi_unsigned_done 		# Check if counter has hit 33
	
	sll	$s0 $s0 1				# Shifts remainder to the left by one to make room
	move	$t3 $s1					# Make copy of quotient to check MSB
	sll	$s1 $s1 1				# Shifts quotient by 1 to the left
	retrieve_nth_bit($t1 $t3 $s5)
	beq	$t1 1 add_one_to_remainder		# Jumps out if MSB of quotient is 1 for special case
	continue_on:
	move	$a0 $s0					# Set $a0 to current remainder to get ready for subtraction
	jal	funcdiff_logi 				# Subtract divisor from remainder
	move	$s4 $v0 				# Copy partial subtraction to temp storage to check
	bltz	$s4 funcdiv_logi_loop_end		# If the difference is negative, branch to shift the divisor right away
	
	shift_divisor_right_pos:
	move	$s0 $s4					# Set remainder as result of positive difference
	addi	$s1 $s1 1				# Adds one to quotient register
	j	funcdiv_logi_loop_end
	
	add_one_to_remainder:
	addi	$s0 $s0 1				# Adds 1 to remainder when MSB of quotient is 1
	j	continue_on				# Jump back to negative case
	
	funcdiv_logi_loop_end:	
	addi	$s3 $s3 1				# Inrements counter by 1
	j	funcdiv_logi_unsigned_looper

func_logi_unsigned_done:
	move	$v0 $s1
	move	$v1 $s0

	lw	$fp 56($sp)
	lw	$ra 52($sp)
	lw	$s0 48($sp)
	lw	$s1 44($sp)
	lw	$s2 40($sp)
	lw	$s3 36($sp)
	lw	$s4 32($sp) 
	lw	$s5 28($sp) 
	lw	$s6 24($sp) 
	lw	$s7 20($sp) 
	lw	$a0 16($sp)
	lw	$a1 12($sp)
	lw	$a2 8($sp)
	addi	$sp $sp 56
	
	jr	$ra
	
#////////////////////////////////////////////////
# Procedure: funcdiv_logi_signed		/
# $a0: 1st argument (Dividend)			/	
# $a1: 2nd argument (Divisor)			/
# Purpose: Output $a1/$a0 to $v0 and remainder  /
# to $v1					/
#////////////////////////////////////////////////	
funcdiv_logi_signed:
	addi	$sp $sp -56
	sw	$fp 56($sp)
	sw	$ra 52($sp)
	sw	$s0 48($sp) # Dividend
	sw	$s1 44($sp) # Divisor
	sw	$s2 40($sp) # Store to check for MSB
	sw	$s3 36($sp) # MSB of dividend
	sw	$s4 32($sp) # MSB of divisor
	sw	$s5 28($sp) # Save remainder
	sw	$s6 24($sp) # Final answer for quotient
	sw	$s7 20($sp) # Final answer for remainder
	sw	$a0 16($sp)
	sw	$a1 12($sp)
	sw	$a2 8($sp)
	addi	$fp $sp 56
	
	move	$s0 $a0 # Save dividend
	move	$s1 $a1 # Save divisor
	move	$t0 $s0 # Temp copy of dividend
	move	$t1 $s1 # Temp copy of divisor
	addi	$s2 $zero 31 # Store to check for MSB 
	
	retrieve_nth_bit($s3 $t0 $s2) # Checks for MSB of dividend
	retrieve_nth_bit($s4 $t1 $s2) # Checks for MSB of divisor
	
	jal	conv_2s_comp_neg 	# Converts dividend to two's complement if needed
	move	$s0 $v0 		# Moves result to saved dividend
	move	$a0 $s1			# Moves saved divisor into $a0 for checking
	jal	conv_2s_comp_neg	# Converts divisor to two's complement if needed
	move	$s1 $v0			# Moves result to saved divisor
	move	$a0 $s0			# Moves converted/ignored dividend to $a0
	move	$a1 $s1			# Moves converted/ignored divisor to $a1

	jal	funcdiv_logi_unsigned	# Execute division
	
	move 	$s5 $v1			# Make copy of remainder	
	add	$t3 $zero $zero
	xor	$t3 $s3 $s4		# XOR between MSB of dividend and divisor to check for sign
	beq	$t3 1 conv_qout_twos_comp	# Checks if the result of the XOR is 1
	move	$s6 $v0			# Saves final answer to $s6 in case MSB is 0
	j	skip_quotient_conv
	
	conv_qout_twos_comp:
	move	$a0 $v0
	jal	conv_2s_comp
	move	$s6 $v0

	skip_quotient_conv:
	bgtz	$s3 conv_remain_twos_comp
	j	funcdiv_logi_signed_done

	conv_remain_twos_comp:
	move	$a0 $s5
	jal	conv_2s_comp
	move	$s5 $v0
	
	funcdiv_logi_signed_done:
	move	$v0 $s6
	move	$v1 $s5
	
	lw	$fp 56($sp)
	lw	$ra 52($sp)
	lw	$s0 48($sp) 
	lw	$s1 44($sp) 
	lw	$s2 40($sp) 
	lw	$s3 36($sp)
	lw	$s4 32($sp) 
	lw	$s5 28($sp) 
	lw	$s6 24($sp) 
	lw	$s7 20($sp) 
	lw	$a0 16($sp)
	lw	$a1 12($sp)
	lw	$a2 8($sp)
	addi	$sp $sp 56
	
	jr	$ra


#////////////////////////////////////////////////
# Procedure: conv_2s_comp			/
# $a0: 1st argument				/	
# Purpose: Convert $a0 to two's complement	/
#////////////////////////////////////////////////
conv_2s_comp:
	addi	$sp $sp -20
	sw	$a0 20($sp)
	sw	$a1 16($sp)
	sw	$ra 12($sp)
	sw	$fp 8($sp)
	addi	$fp $sp 16
	
	not 	$a0 $a0
	addi	$a1 $zero 1
	jal	funcsum_logi
	
	lw	$a0 20($sp)
	lw	$a1 16($sp)
	lw	$ra 12($sp)
	lw	$fp 8($sp)
	addi	$sp $sp 20
	
	jr	$ra
	
#////////////////////////////////////////////////
# Procedure: conv_2s_comp_neg			/
# $a0: 1st argument				/	
# Purpose: Convert $a0 to two's complement if it/
# is negative					/
#////////////////////////////////////////////////
conv_2s_comp_neg:
	addi	$sp $sp -16
	sw	$a0 16($sp)
	sw	$ra 12($sp)
	sw	$fp 8($sp)
	addi	$fp $sp 16
	
	bgez	$a0 preserve_2s_comp
	jal	conv_2s_comp
	j	conv_2s_comp_neg_done
	
	preserve_2s_comp:
	move	$v0 $a0

	conv_2s_comp_neg_done:
	lw	$a0 16($sp)
	lw	$ra 12($sp)
	lw	$fp 8($sp)
	addi	$sp $sp 16
	
	jr	$ra
	
#////////////////////////////////////////////////
# Procedure: conv_2s_comp_sixfour		/
# $a0: 1st argument				/	
# Purpose: Convert $a0 to two's complement, with/
# LO part in $v0 and HI part in $v1		/
#////////////////////////////////////////////////
conv_2s_comp_sixfour:
	addi	$sp $sp -56
	sw	$fp 56($sp)
	sw	$ra 52($sp)
	sw	$s0 48($sp) 
	sw	$s1 44($sp) 
	sw	$s2 40($sp) 
	sw	$s3 36($sp) 
	sw	$s4 32($sp) 
	sw	$s5 28($sp)
	sw	$s6 24($sp) 
	sw	$s7 20($sp) 
	sw	$a0 16($sp)
	sw	$a1 12($sp)
	sw	$a2 8($sp)
	addi	$fp $sp 56
	
	not 	$a0 $a0		# Negates LO
	not	$a1 $a1 	# Negates HI
	move	$s3 $a1		# Saves HI to $s3
	move	$s0 $a0		# Saves LO to $s0
	
	addi	$a1 $zero 1	# Sets HI to 1
	jal	funcsum_logi
	move	$s1 $v0		# Sets sum to $s1
	move	$a0 $v1		# Sets LO to overflow bit
	
	move	$a1 $s3		# Sets HI to saved LO
	
	jal	funcsum_logi
	
	move	$v1 $v0
	move	$v0 $s1
	
	lw	$fp 56($sp)
	lw	$ra 52($sp)
	lw	$s0 48($sp) 
	lw	$s1 44($sp) 
	lw	$s2 40($sp) 
	lw	$s3 36($sp)
	lw	$s4 32($sp) 
	lw	$s5 28($sp) 
	lw	$s6 24($sp) 
	lw	$s7 20($sp) 
	lw	$a0 16($sp)
	lw	$a1 12($sp)
	lw	$a2 8($sp)
	addi	$sp $sp 56
	
	jr	$ra
