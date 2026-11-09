if (inactive)
{
    report 1 / 0
}
# Comments and blank lines do not detach else.

else
{
    report 4 # expect: 4
}
