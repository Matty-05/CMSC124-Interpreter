unit hp = 100
{
    unit hp = 40
    unit armor = 5
    access_point "training"
    unit pod = 1
    {
        hp = hp - 10
        armor = 0
        unit local = 7
        scan "training"
        # expect: [Pod 042] pod: <undefined> -> 1
        # expect: [Pod 042] hp: 40 -> 30
        # expect: [Pod 042] armor: 5 -> 0
        restore "training"
        # expect: [Pod 042] hp: 30 -> 40
        # expect: [Pod 042] armor: 0 -> 5
        # expect: [Pod 042] pod: 1 -> <undefined>
        report local # expect: 7
    }
    report hp # expect: 40
}
report hp # expect: 100
