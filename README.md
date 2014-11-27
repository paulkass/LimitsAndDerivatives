LimitsAndDerivatives
====================

A library of Java classes to process functions and find limits and derivatives. 

Prerequisites
=============

1) MySql Database
2) JDBC Driver
    link to download: https://dev.mysql.com/downloads/connector/j/
    
Use
===

1) Supports the following "special" functions:
    sin, cos, tan, asin, acos, atan, abs, ln, log, and sign
    To add more to the database, run the command in the following format
        insert into special_functions values ("<Name>", "<Replacement>")
        use the # sign to represent the argument of the function in the Replacement
        
2) Parentheses should be used when dealing with "special" functions and raising an expression to a power.
3) When raising an expression to a power, ALWAYS use parentheses. For example, x^2 won't work but x^(2) will work.

Those are the basic things.
If you have questions, comments, or you just want to report a bug, email me here: pkassian@mail.ccsf.edu
Have Fun!
