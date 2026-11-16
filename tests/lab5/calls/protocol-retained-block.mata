unit saved = void
{
    unit message = "retained"
    protocol read() { transmit message }
    saved = read
}
report saved() # expect: retained
