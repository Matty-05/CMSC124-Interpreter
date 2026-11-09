unit hp = 100
{
    unit hp
    report hp # expect: void
    hp = 40
    report hp # expect: 40
}
report hp # expect: 100
