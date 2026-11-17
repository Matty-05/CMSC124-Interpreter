protocol recurse() { transmit recurse() }
recurse() # expect runtime error: Stack overflow: protocol calls nested too deeply.
