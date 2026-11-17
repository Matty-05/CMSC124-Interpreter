{
    protocol local() { transmit 3 }
    report local() # expect: 3
}
