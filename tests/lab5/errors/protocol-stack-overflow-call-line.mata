# The overflow is reported on the line of the call that started it, not line 3.
# expect runtime error: [line 7] Runtime error: Stack overflow: protocol calls nested too deeply.
protocol recurse() {
  transmit recurse()
}

recurse()
