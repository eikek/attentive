module Util.Time exposing (..)

import DateFormat
import Time exposing (Posix, Zone, utc)


dateFormatter : Zone -> Posix -> String
dateFormatter =
    DateFormat.format
        [ DateFormat.dayOfWeekNameAbbreviated
        , DateFormat.text ", "
        , DateFormat.monthNameFull
        , DateFormat.text " "
        , DateFormat.dayOfMonthSuffix
        , DateFormat.text ", "
        , DateFormat.yearNumber
        ]

timeFormatter: Zone -> Posix -> String
timeFormatter =
    DateFormat.format
        [ DateFormat.hourMilitaryNumber
        , DateFormat.text ":"
        , DateFormat.minuteFixed
        ]

timeZone: Zone
timeZone =
    utc

{- Format millis into "Wed, 10. Jan 2018, 18:57"
-}
formatDateTime: Int -> String
formatDateTime millis =
    (formatDate millis) ++ ", " ++ (formatTime millis)

{- Format millis into "18:57"
-}
formatTime: Int -> String
formatTime millis =
    Time.millisToPosix millis
        |> timeFormatter timeZone

{- Format millis into "Wed, 10. Jan 2018"
-}
formatDate: Int -> String
formatDate millis =
    Time.millisToPosix millis
        |> dateFormatter timeZone
