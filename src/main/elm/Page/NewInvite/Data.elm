module Page.NewInvite.Data exposing (..)

import Http
import Api
import Data.Flags exposing (Flags)
import Data.Invitation exposing (Invitation)

type alias Model =
    { flags: Flags
    , invpass: String
    , invite: Maybe Invitation
    , error: Maybe String
    , process: Bool
    }

emptyModel: Flags -> Model
emptyModel flags =
    { flags = flags
    , invpass = ""
    , invite = Nothing
    , error = Nothing
    , process = False
    }


type Msg
    = SetPassword String
    | Generate
    | GenerateResp (Result Http.Error Invitation)
