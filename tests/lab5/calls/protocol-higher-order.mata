protocol identity(value) { transmit value }
protocol addOne(value) { transmit value + 1 }
protocol apply(operation, value) { transmit operation(value) }
report apply(addOne, 4) # expect: 5
report identity(addOne)(9) # expect: 10
