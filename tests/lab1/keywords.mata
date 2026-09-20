unit x = active
unit iffy = inactive
unit nothing = void

protocol check(a, b) {
  if a and b {
    transmit active
  } else {
    transmit not a or b
  }
}

while x {
  for x {
    scan x
  }
}

report x