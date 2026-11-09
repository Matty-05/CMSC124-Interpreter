if (inactive and 1 / 0) { report "wrong" } else { report 1 } # expect: 1
if (active or 1 / 0) { report 2 } # expect: 2
