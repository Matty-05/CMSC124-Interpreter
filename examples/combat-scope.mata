# Simulate a shielded hit, then a normal hit.
unit hp = 100
unit damage = 30

{
    # This hit has its own damage and armor; base damage stays unchanged.
    unit damage = 20
    unit armor = 5
    {
        unit hit = damage - armor
        hp = hp - hit
    }
    report "HP after shielded hit:" # expect: HP after shielded hit:
    report hp # expect: 85
}

report "Base damage:" # expect: Base damage:
report damage # expect: 30
hp = hp - damage
report "HP after normal hit:" # expect: HP after normal hit:
report hp # expect: 55
