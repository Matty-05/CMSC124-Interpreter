if (0) { report "zero" } # expect: zero
if ("") { report "empty" } # expect: empty
if (void) { report "wrong" } else { report "void" } # expect: void
