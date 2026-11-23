access_point "empty"
unit hp = 80
restore "empty"
# expect: [Pod 042] Restoring "empty":
# expect: [Pod 042] hp: 80 -> <undefined>
report hp # expect runtime error: Undefined variable 'hp'.
