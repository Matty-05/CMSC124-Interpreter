unit hp = 100
{
    unit hp = 40
    {
        hp = hp - 10
        report hp # expect: 30
    }
    report hp # expect: 30
}
report hp # expect: 100
