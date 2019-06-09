module Data.Registration exposing (..)

import Json.Decode as Decode exposing (Decoder, int, string, float, bool)
import Json.Decode.Pipeline exposing (required, optional, hardcoded)
import Json.Encode as Encode

type alias Registration =
    { name: String
    , password: String
    , invitation: String
    }

decoder: Decoder Registration
decoder =
    Decode.succeed Registration
        |> required "name" string
        |> required "password" string
        |> required "invitation" string

encode: Registration -> Encode.Value
encode r =
    Encode.object
        [ ("name", Encode.string r.name)
        , ("password", Encode.string r.password)
        , ("invitation", Encode.string r.invitation)
        ]

