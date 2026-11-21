# access_point, restore and hack are keywords, so they cannot name a unit.
unit hack = 1 # expect error: [line 2] Error at 'hack': Expect unit name.
unit restore = 1 # expect error: [line 3] Error at 'restore': Expect unit name.
unit access_point = 1 # expect error: [line 4] Error at 'access_point': Expect unit name.
