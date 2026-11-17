protocol bare() { transmit }
protocol empty() {}
protocol fallsThrough() { unit x = 1 }
report bare() # expect: void
report empty() # expect: void
report fallsThrough() # expect: void
