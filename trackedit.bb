;---------------------------------------------------------------------
; F-Zero Climax Track editor / Password Maker
; Written and Copyrighted 2004 by CaitSith2
;---------------------------------------------------------------------


AppTitle "F-Zero CTE"
Global verstr$="1.10"

Include "ImageMaster.bb"

Global trackpieceimage=LoadPack("images\TrackPieces.png")

w=ClientWidth(Desktop())
h=ClientHeight(Desktop())
Global window=CreateWindow( "F-Zero Climax Track Editor",(w-640)/2,(h-570)/2,640,570,0,5)
;Add some menus
Global menu=CreateMenu( "&File",0,WindowMenu(window) )
CreateMenu "&New",1,menu
CreateMenu "",2,menu
CreateMenu "Save Password Bin File", 7,menu
CreateMenu "Save Course",5,menu
CreateMenu "Load Course",6,menu
CreateMenu "Export Image",4,menu
CreateMenu "",2,menu
CreateMenu "E&xit",3,menu
Global editmenu=CreateMenu( "&Edit",0,WindowMenu(window) )
Global editundo=CreateMenu( "&Undo",10,editmenu)
Global editredo=CreateMenu( "&Redo",13,editmenu)
CreateMenu "",2,editmenu
Global checkoverride=CreateMenu ("Override Password Checksum", 11,editmenu)
Global fixedoverride=CreateMenu ("Override Fixed Byte", 12, editmenu)
DisableMenu editundo
Global menu2=CreateMenu( "&Help",0,WindowMenu(window))
CreateMenu( "&About",40,menu2)
CreateMenu( "", 2, menu2)
CreateMenu( "Check Version",41,menu2)
UpdateWindowMenu window

Global win = CreateCanvas(0,0,640,305,window)
Global piecewin = CreateCanvas(112,310,48,48,window)

Global label1 = CreateLabel("Current Piece",10,310,100,12,window,0)
Global pointlabel = CreateLabel("Points: 0",10,325,100,12,window,0)
Global scorelabel = CreateLabel("Score: 0/255",170,310,100,12,window,0)
Global loopcompletelabel = CreateLabel("Start Piece Required",170,325,100,12,window,0)
Global loopscorelabel = CreateLabel("",170,340,150,12,window,0)

Global mode1but = CreateButton ("1x1 Pieces",10,340,95,20,window,1)
Global mode2but = CreateButton ("2x2 Pieces",10,360,95,20,window,1)
Global mode3but = CreateButton ("3x3 Pieces Set 1",10,380,95,20,window,1)
Global mode4but = CreateButton ("3x3 Pieces Set 2",10,400,95,20,window,1)
Global passbut = CreateButton ("Get Password",10,440,95,20,window,1)
Global loadpassbut = CreateButton ("Load Password",10,460,95,20,window,1)
Global loadrecbut = CreateButton ("Load Record",10,480,95,20,window,1)
Global undobut = CreateButton ("Undo",10,420,47,20,window,1)
;Global undobut = CreateButton ("Undo",10,420,95,20,window,1)
Global redobut = CreateButton ("Redo",58,420,47,20,window,1)
;HideGadget redobut



Global namelabel = CreateLabel("Course Name",350,310,100,12,window,0)
Global nametext = CreateTextField(450,310,100,20,window)

Global planetlabel = CreateLabel("Planet",350,330,140,15,window,0)
Global planetcombo = CreateComboBox(450,330,140,20,window)

Global drivernamelabel = CreateLabel("Driver: C.FALCON", 110, 480, 100, 15, window, 0)
Global drivercarlabel = CreateLabel("Car: Blue Falcon",210,480,200,15,window,0)
Global recordlabel = CreateLabel("Record: 4'59" + Chr$(34) + "97",410,480,100,15,window,0)

AddGadgetItem planetcombo, "Mute City"
AddGadgetItem planetcombo, "Big Blue"
AddGadgetItem planetcombo, "Sand Ocean (calm)"
AddGadgetItem planetcombo, "Silence"
AddGadgetItem planetcombo, "Port Town"
AddGadgetItem planetcombo, "Lightning"
AddGadgetItem planetcombo, "Red Canyon"
AddGadgetItem planetcombo, "Fire Field"
AddGadgetItem planetcombo, "White Land"
AddGadgetItem planetcombo, "Mist Flow"
AddGadgetItem planetcombo, "Illusion"
AddGadgetItem planetcombo, "Sand Ocean (stormy)"
SelectGadgetItem planetcombo, 0

Global passtextarea = CreateTextArea(110,360,500,120,window)

Global recordtimeS=0
Global recordtimeMS=0
Global vehicle=0
Global driver$=""
Global editflag=False


SetBuffer CanvasBuffer(win)


Type piece
	Field gfx
	Field Value
	Field point
	Field ptype
	Field ent
	Field ext
End Type

Type undo
	Field x
	Field y
	Field value
	Field prevundo
End Type

Dim pieces.piece(255)
Dim grid(18,18)
Dim loopgrid(16,16)
Dim scoregrid(16,16)
Global blankpiece
Dim undobuf.undo(9999)
Dim redobuf.undo(9999)
Global undocount=0
Global undoflag=False
Global redocount=0
Global redoflag=False

Global checkloopflag=True
Global checkloopscoreflag=True

Global createpassword_err

Restore Data1x1

initgfx

scratcharea()


SeedRnd MilliSecs()
i=Rnd(1,255)
;debuglog "Value = " + pieces(i)\Value + " points = " + pieces(i)\point + " Type = " + pieces(i)\ptype + "x" + pieces(i)\ptype
;While Not KeyHit(1)
;	Cls
;	DrawImage pieces(i)\gfx,MouseX(),MouseY()
;	Flip
;Wend




mode = 1
setmode(mode)


Repeat

If (undocount<>0)
	EnableMenu editundo
	EnableGadget undobut
Else
	DisableMenu editundo
	DisableGadget undobut
End If
If (redocount<>0)
	EnableMenu editredo
	EnableGadget redobut
Else
	DisableMenu editredo
	DisableGadget redobut
End If
UpdateWindowMenu window

event = WaitEvent()
evsrc = EventSource()
evID = EventID()
evdata = EventData()

x = MouseX(win)
y = MouseY(win)
If(MouseHit(1)>0) Then 
	but = 1
ElseIf(MouseHit(2)>0) Then
	but = 2
Else
	but = 0
End If
;but = GetMouse()
drawx = (x-(16*20))/16
drawy = ((y)/16)

	Select evID
		Case $401
			Select evsrc
				Case mode1but
					mode = 1
					setmode(1)
				Case mode2but
					mode = 2
					setmode(2)
				Case mode3but
					mode = 3
					setmode(3)
				Case mode4but
					mode = 4
					setmode(4)
				Case passbut
					SetGadgetText passtextarea, ""
					passstr$=createpassword()
					passstrlen = Len(passstr$)
					For i = 1 To passstrlen
						AddTextAreaText passtextarea, Mid$(passstr$,i,1)
						If ((i Mod 4) = 0) And ((i Mod 20) <> 0) Then
							AddTextAreaText passtextarea, " "
						ElseIf ((i Mod 20) = 0) Then
							AddTextAreaText passtextarea, Chr$(13) + Chr$(10)
						End If
					Next ;i
					If(passstrlen = 0) Then
						Select createpassword_err
							Case 2
								Notify "Your Course Name cannot be blank or just spaces"
							Case 3
								Notify "Your course name contains an invalid character" + Chr$(13) + "Valid Characters are 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!?,.-'&_@ '"
						End Select
					End If
					SetGadgetText drivernamelabel, "Driver: " + driver$
					SetGadgetText drivercarlabel, "car: " + getcarname$(vehicle)
					SetGadgetText recordlabel, "Record: " + ((recordtimeS) Shr 5) + "'" + ((((recordtimeS Shl 27) Shr 27)*2) + (recordtimeMS Shr 7)) + Chr$($22) + ((recordtimeMS Shl 25) Shr 25)
				Case undobut
					undofunction()
				Case redobut
					redofunction()
				Case loadpassbut
					loadpassword(TextAreaText$(passtextarea),True,False)
					SetGadgetText drivernamelabel, "Driver: " + driver$
					SetGadgetText drivercarlabel, "car: " + getcarname$(vehicle)
					SetGadgetText recordlabel, "Record: " + ((recordtimeS) Shr 5) + "'" + ((((recordtimeS Shl 27) Shr 27)*2) + (recordtimeMS Shr 7)) + Chr$($22) + ((recordtimeMS Shl 25) Shr 25)
					editflag = False
				Case loadrecbut
					loadpassword(TextAreaText$(passtextarea),True,True)
					SetGadgetText drivernamelabel, "Driver: " + driver$
					SetGadgetText drivercarlabel, "car: " + getcarname$(vehicle)
					SetGadgetText recordlabel, "Record: " + ((recordtimeS) Shr 5) + "'" + ((((recordtimeS Shl 27) Shr 27)*2) + (recordtimeMS Shr 7)) + Chr$($22) + ((recordtimeMS Shl 25) Shr 25)
					editflag = False
			End Select
		Case $1001
			Select evdata
				Case 1 ; File / New
					If Confirm("Are you sure you want to Start a new track?"+Chr$(13)+"This Action is not undoable")
						resetgrid()
					End If
				Case 3 ; File / Exit
					
					If Confirm("Do you really want to Exit. ?")
						programcleanup()
						End
					EndIf
					
				Case 4 ; File / Export Image
					ExportImage()
				Case 5 ; File / Save Course
					checkloopflag = False
					checkloopscoreflag = False
					
					SetGadgetText passtextarea, ""
					passstr$=createpassword()
					passstrlen = Len(passstr$)
					SaveFileName$ = RequestFile$( "Save As", "trk", True)
					fileout = WriteFile(SaveFileName$)
					If(fileout<>0)Then
						WriteString(fileout,passstr$)
						WriteShort(fileout,undocount)
						For i = 1 To undocount
							WriteByte (fileout, undobuf(i)\x)
							WriteByte (fileout, undobuf(i)\y)
							WriteByte (fileout, undobuf(i)\value)
							WriteByte (fileout, undobuf(i)\prevundo)
						Next ;i
						
						WriteShort(fileout,redocount)
						For i = 1 To redocount
							WriteByte (fileout, redobuf(i)\x)
							WriteByte (fileout, redobuf(i)\y)
							WriteByte (fileout, redobuf(i)\value)
							WriteByte (fileout, redobuf(i)\prevundo)
						Next ;i
						
						
						CloseFile(fileout)
					End If				
					checkloopflag = True
					checkloopscoreflag = True
				Case 6 ; File / Load Course
					OpenFileName$ = RequestFile$( "Load Course", "trk", False)
					filein = ReadFile(OpenFileName$)
					If(filein<>0)Then
						passstrlen=ReadInt(filein)
						If(passstrlen>0 And passstrlen<641) Then
							SeekFile(filein,0)
							passstr$ = ReadString$(filein)
							err=loadpassword(passstr$,False)
							If (err=0) Then 
								undocount = ReadShort(filein)
								For i = 1 To undocount
									u.undo = New undo
									u\x = ReadByte(filein)
									u\y = ReadByte(filein)
									u\value = ReadByte(filein)
									u\prevundo = ReadByte(filein)
									undobuf(i) = u
								Next ;i
								redocount = ReadShort(filein)
								For i = 1 To redocount
									u.undo = New undo
									u\x = ReadByte(filein)
									u\y = ReadByte(filein)
									u\value = ReadByte(filein)
									u\prevundo = ReadByte(filein)
									redobuf(i) = u
								Next ;i
							Else
								Notify "This Track File is corrupted"
							End If
						Else
							Notify "File is not a Track File"
						End If
						CloseFile(filein)
					EndIf
				Case 7 ; File / Save Password Bin File
					passstr$=createpassword()
					If(Len(passstr$)>0) Then
						SaveFileName$ = RequestFile$( "Save As", "dmp", True)
						fileout=WriteFile(saveFileName$)
						If(fileout<>0) Then
							passlen = Len(passstr$)
							For i = 1 To passlen
								WriteByte(fileout,Asc(Mid$(passstr$,i,1))-32)
							Next ;i
							CloseFile(fileout)
						End If
					End If
				Case 10 ;Edit / Undo
					undofunction()
				Case 11 ;Edit / Checksum Override
					If (MenuChecked(checkoverride)) Then
						UncheckMenu(checkoverride)
					Else
						CheckMenu(checkoverride)
					End If
				Case 12 ; Edit / Fixed Byte Override
					If (MenuChecked(fixedoverride)) Then
						UncheckMenu(fixedoverride)
					Else
						CheckMenu(fixedoverride)
					End If
					
				Case 13 ; Edit / Redo
					redofunction()
				Case 40 ;Help / About
					Notify "F-Zero Climax Track Editor"+Chr$(13)+"version "+verstr$+Chr$(13)+"by CaitSith2"
				Case 41 ;Help / Check Version
					version=checkversion()
					If(version=0) Then
						Notify "There is a Newer Version Available"
					ElseIf (version=1) Then
						Notify "You have the most recent version"
					End If
			End Select
	End Select
	
	If(but=2) Then
		mode = mode + 1
		If (mode = 5) Then mode = 1
		setmode(mode)
	End If
	
	

	Select mode
		Case 1
			selectx = ((x - 16) / 16)
			selecty = ((y - 16) / 16)
			If(selectx>=0 And selectx<=7 And selecty>=0 And selecty<=15 And but=1) Then
				tile = (selecty * 8) + selectx
			EndIf
			
		Case 2
			selectx = ((x - 16) / 32)
			selecty = ((y - 16) / 32)
			If(selectx>=0 And selectx<=7 And selecty>=0 And selecty<=8 And but=1) Then
				tile = (selecty * 8) + selectx
				If(tile>59) Then tile = 59
				tile = tile + 128
			EndIf
		Case 3
			selectx = ((x - 16) / 48)
			selecty = ((y - 16) / 48)
			If(selectx>=0 And selectx<=5 And selecty>=0 And selecty<=5 And but=1) Then
				tile = (selecty * 6) + selectx
				tile = tile + 188
			EndIf
		Case 4
			selectx = ((x - 16) / 48)
			selecty = ((y - 16) / 48)
			If(selectx>=0 And selectx<=5 And selecty>=0 And selecty<=5 And but=1) Then
				tile = (selecty * 6) + selectx
				If(tile>31) Then tile = 31
				tile = tile + 224
			EndIf
	End Select
	SetBuffer CanvasBuffer(piecewin)
	Color 0, 0, 0
	Rect 0,0,48,48
	If(tile<>0) Then
		Select pieces(tile)\ptype
			Case 1
				DrawImage pieces(tile)\gfx,16,16
			Case 2
				DrawImage pieces(tile)\gfx,8,8
			Case 3
				DrawImage pieces(tile)\gfx,0,0
		End Select
		
		SetGadgetText pointlabel, "Points: " + pieces(tile)\point
		
		
	Else
		SetGadgetText pointlabel, "Points: 0"
;		DrawImage blankpiece,32,(16*19)	
	EndIf
	FlipCanvas piecewin
	SetBuffer CanvasBuffer(win)
	

	If(drawx>=1 And drawx<=16 And drawy>=1 And drawy<=16 And but=1) Then
		
		If (tile = 0) Then
			
			drawpiece drawx,drawy,Null
		Else
			drawpiece drawx,drawy,pieces(tile)
		EndIf
		refreshscore()
		;DebugLog "checkloop = " + result + " score = " + score
	EndIf
	

;	DrawImage pieces(i)\gfx,
	FlipCanvas win
Until event=$803


programcleanup

End

Function ExportImage()
	image=CreateImage(256,256)
	SetBuffer ImageBuffer(image)
	For x = 1 To 16
		For y = 1 To 16
			If (scoregrid(x,y)=1) Then
				DrawImage pieces(grid(x,y))\gfx,(x-1)*16,(y-1)*16
			End If
		Next ;y
	Next ;x
	SetBuffer CanvasBuffer(win)
	SaveFileName$ = RequestFile$( "Save As", "bmp", True )
	ok=SaveImage(image,SaveFileName$)
	FreeImage(image)
	Return ok
End Function

Function DumpTiles()
	image=CreateImage(16,16)
	SetBuffer ImageBuffer(image)
	DrawImage blankpiece,0,0
	SaveImage(image,"images\x00.bmp")
	SetBuffer CanvasBuffer(win)

	For x = 1 To 127
		SetBuffer ImageBuffer(image)
        DrawImage pieces(x)\gfx, 0, 0
		SaveImage(image,"images\x" + Lower$(Right$(Hex$(x),2)) + ".bmp")
		SetBuffer CanvasBuffer(win)
	Next  ;x
	FreeImage(image)
	image=CreateImage(32,32)
	For x = 128 To 187
        SetBuffer ImageBuffer(image)
        DrawImage pieces(x)\gfx, 0, 0
		SaveImage(image,"images\x" + Lower$(Right$(Hex$(x),2)) + ".bmp")
		SetBuffer CanvasBuffer(win)
	Next  ;x
	FreeImage(image)
	image=CreateImage(48,48)
	For x = 188 To 255
        SetBuffer ImageBuffer(image)
        DrawImage pieces(x)\gfx, 0, 0
		SaveImage(image,"images\x" + Lower$(Right$(Hex$(x),2)) + ".bmp")
		SetBuffer CanvasBuffer(win)
	Next  ;x
	FreeImage(image)
	Return 0
End Function

Function redofunction()
	redoflag = True
	If(redocount<>0)
		Repeat
			tempprevundo = redobuf(redocount)\prevundo
			;debuglog "x = " + redobuf(redocount)\x + " y = " + redobuf(redocount)\y + " piece = " + redobuf(redocount)\value + " tmpvalue = " + tmpvalue
			If (redobuf(redocount)\value <> 0) Then
				Drawpiece redobuf(redocount)\x, redobuf(redocount)\y, pieces(redobuf(redocount)\value)
			Else
				Drawpiece redobuf(redocount)\x, redobuf(redocount)\y, Null
			End If
			Delete redobuf(redocount)
			redocount = redocount - 1
		Until ((tempprevundo = False) Or (redocount = 0))
	End If
	redoflag = False
	refreshscore()
End Function


Function undofunction()
	undoflag = True
	tmpcount = 0
	If(undocount<>0)
		Repeat
			tempprevundo = undobuf(undocount)\prevundo
			;debuglog "x = " + undobuf(undocount)\x + " y = " + undobuf(undocount)\y + " piece = " + undobuf(undocount)\value + " tmpvalue = " + tmpvalue
			If (undobuf(undocount)\value <> 0) Then
				Drawpiece undobuf(undocount)\x, undobuf(undocount)\y, pieces(undobuf(undocount)\value)
			Else
				Drawpiece undobuf(undocount)\x, undobuf(undocount)\y, Null
			End If
			Delete undobuf(undocount)
			undocount = undocount - 1
			tmpcount = tmpcount + 1
		Until ((tempprevundo = False) Or (undocount = 0))
	End If
	i = tmpcount
	While (i > 1)
;		redobuf((redocount-tmpcount)+i)\prevundo = True
		i = i - 1
	Wend
	undoflag = False
	refreshscore()
End Function

Function refreshscore()
	result = checkloop()
	Select result
		Case -1
			SetGadgetText loopcompletelabel, "Start Piece Required"
			SetGadgetText loopscorelabel, ""
		Case -2
			SetGadgetText loopcompletelabel, "Only one Start Piece Allowed"
			SetGadgetText loopscorelabel, ""
		Case 0
			SetGadgetText loopcompletelabel, "Loop Incomplete"
			SetGadgetText loopscorelabel, ""
		Case 1
			SetGadgetText loopcompletelabel, "Loop Complete"
			SetGadgetText loopscorelabel, "Loop Score: " + checkloopscore() + "/255"
			;createpassword()
	End Select
	score = checktotalscore()
	SetGadgetText scorelabel, "Score: " + score + "/255"
End Function

Function getcarname$(car)
	Select car
		Case $00
			Return "Blue Falcon"
		Case $01
			Return "Golden Fox"
		Case $02
			Return "Wild Goose"
		Case $03
			Return "Fire Stingray"
		Case $04
			Return "White Cat"
		Case $05
			Return "Red Gazelle"
		Case $06
			Return "Iron Tiger"
		Case $07
			Return "Deep Claw"
		Case $08
			Return "Crazy Bear"
		Case $09
			Return "Great Star"
		Case $0A
			Return "Big Fang"
		Case $0B
			Return "Mad Wolf"
		Case $0C
			Return "Night Thunder"
		Case $0D
			Return "Twin Noritta"
		Case $0E
			Return "Wonder Wasp"
		Case $0F
			Return "Queen Meteor"
		Case $10
			Return "Blood Hawk"
		Case $11
			Return "Astro Robin"
		Case $12
			Return "Little Wyvern"
		Case $13
			Return "Death Anchor"
		Case $14
			Return "Wild Boar"
		Case $15
			Return "King Meteor"
		Case $16
			Return "Super Piranha"
		Case $17
			Return "Mighty Hurricane"
		Case $18
			Return "Space Angler"
		Case $19
			Return "Mighty Typhoon"
		Case $1A
			Return "Hyper Speeder"
		Case $1B
			Return "Green Panther"
		Case $1C
			Return "Black Bull"
		Case $1D
			Return "Sonic Phantom"
		Case $1E
			Return "Dragon Bird"
		Case $1F
			Return "Panzer Emerald"
		Case $20
			Return "Elegance Liberty"
		Case $21
			Return "Moon Shadow"
		Case $22
			Return "Soldier Anchor"
		Case $23
			Return "Red Bull"
		Case $24
			Return "Dragon Bird GP"
		Case $25
			Return "Blue Falcon GP"
		Case $26
			Return "Hyper Death Anchor"
			
			
		Default
			Return "Unknown Car - $" + Right$(Hex$(car),2)
		
	End Select
End Function

Function setmode(mode)
	Color 0, 0, 0
	Rect 16,16,288,288,1
	Select mode
		Case 1
			DrawImage blankpiece,16,16
			For i = 1 To 127
				x=(((i) Mod 8) * 16) + 16
				y=(((i) / 8) * 16) + 16
				DrawImage pieces(i)\gfx,x,y
			Next ;i
		Case 2
			For i = 128 To 187
				x=(((i-128) Mod 8) * 32) + 16
				y=(((i-128) / 8) * 32) + 16
				DrawImage pieces(i)\gfx,x,y
			Next ;i
		Case 3
			For i = 188 To 223
				x=(((i-188) Mod 6) * 48) + 16
				y=(((i-188) / 6) * 48) + 16
				DrawImage pieces(i)\gfx,x,y
			Next ;i
		Case 4
			For i = 224 To 255
				x=(((i-224) Mod 6) * 48) + 16
				y=(((i-224) / 6) * 48) + 16
				DrawImage pieces(i)\gfx,x,y
			Next ;i
	End Select
End Function

Function resetgrid()
	For i = 1 To 16
		For j = 1 To 16
			Drawpiece(i,j,Null)
		Next ;j
	Next ;i
	SetGadgetText scorelabel, "Score: 0/255"
	SetGadgetText loopcompletelabel, "Start Piece Required"
	SetGadgetText loopscorelabel, ""
	SetGadgetText nametext, ""
	SetGadgetText passtextarea, ""
	SelectGadgetItem planetcombo, 0
	SetGadgetText drivernamelabel, "Driver: C.FALCON"
	SetGadgetText drivercarlabel, "Car: Blue Falcon"
	SetGadgetText recordlabel, "Record: 4'59" + Chr$(34) + "97"
	
	clearundobuffer()
	clearredobuffer()
End Function

Function clearundobuffer()
	For i = 1 To undocount
		Delete undobuf(i)
	Next ;i
	undoflag = False
	undocount=0
End Function

Function clearredobuffer()
	For i = 1 To redocount
		Delete redobuf(i)
	Next ;i
	redocount = 0
	redoflag = False
End Function

Function Drawpiece(px,py,p.piece)
	
	editflag = True
	x = px
	y = py
	If (p <> Null)
		Select p\ptype
			Case 1
				maxxy = 16
			Case 2
				maxxy = 15
			Case 3
				maxxy = 14
		End Select
	Else
		maxxy = 16
	End If
	
	If(x > maxxy) Then x = maxxy
	If(y > maxxy) Then y = maxxy
	
	prevvalue = grid(x,y) + 65280
	If(p<>Null) Then currvalue = p\value + 65280
	xx = ((20+x)*16)
	yy = (y*16)
	checkgrid(x,y,p)
	
	If (p = Null) Then
		DrawImage blankpiece, xx, yy
		grid(x,y)=0
		scoregrid(x,y)=0
	Else
		scoregrid(x,y)=1
		DrawImage p\gfx, ((20+x)*16),(y*16)
		Select p\pType
			Case 1
				grid(x,y)=p\value
			Case 2
				grid(x,y)=p\value
				grid(x+1,y)=currvalue
				grid(x,y+1)=p\value+256
				grid(x+1,y+1)=currvalue
			Case 3
				Select p\value
					Case 200
						If(grid(x,y+1)<>0) Then drawpiece(x,y+1,pieces(grid(x,y+1)))
						If(grid(x+1,y+1)<>0) Then drawpiece(x+1,y+1,pieces(grid(x+1,y+1)))
						If(grid(x+2,y+1)<>0) Then drawpiece(x+2,y+1,pieces(grid(x+2,y+1)))
						;grid(x+1,y+1)=0
						;grid(x+2,y+1)=0
					Case 202
						If(grid(x,y+1)<>0) Then drawpiece(x,y+1,pieces(grid(x,y+1)))
						If(grid(x+1,y+1)<>0) Then drawpiece(x+1,y+1,pieces(grid(x+1,y+1)))
						If(grid(x+2,y+1)<>0) Then drawpiece(x+2,y+1,pieces(grid(x+2,y+1)))
						;grid(x,y+1)=0
						;grid(x+1,y+1)=0
						;grid(x+2,y+1)=0
					Default
						grid(x,y+1)=p\value+256
						grid(x+1,y+1)=currvalue
						grid(x+2,y+1)=currvalue
				End Select
				grid(x,y)=p\value
				grid(x+1,y)=currvalue
				grid(x+2,y)=currvalue
				grid(x,y+2)=p\value+512
				grid(x+1,y+2)=currvalue
				grid(x+2,y+2)=currvalue
		End Select
	EndIf
	
End Function

Global firstundo

Function checkgrid(x,y,p.piece,recurse=False)
	If (recurse = False) firstundo = True
;	Print "recurse = " + recurse
	Color 0, 0, 0
	xx = ((20+x)*16)
	yy = (y*16)
	tempx = x
	tempy = y
	gridpiece = grid(x,y) Shr 8
	;debuglog "gridpiece = " + gridpiece
	If(gridpiece=255) Then
		;debuglog "true"
		xx = xx - 16
		tempx = x - 1
		gridpiece=grid(x-1,y) Shr 8
		If(gridpiece=255) Then
			;debuglog "true"
			xx = xx - 16
			tempx = x - 2
			gridpiece=grid(x-2,y) Shr 8
			tempy = y - gridpiece
			yy = yy - (gridpiece * 16)
			gridpiece=grid(x-2,y-gridpiece)
		Else
			;debuglog "false"
			tempy = y - gridpiece
			yy = yy - (gridpiece * 16)
			gridpiece=grid(x-1,y-gridpiece)
		EndIf
	Else
		;debuglog "false"
		tempy = y - gridpiece
		yy = yy - (gridpiece * 16)
		gridpiece=grid(x,y-gridpiece)
	EndIf
	
	createundo = False
	If(p<>Null) Then
		If(p\Value <> grid(x,y)) Then createundo = True
	Else
		If(grid(x,y) <> 0) Then createundo = True
	End If
	
	If(createundo = True)
		If((undoflag = False) And (undocount < 9999)) Then
			If(redoflag = False) clearredobuffer()
			u.undo = New undo
			u\x = tempx
			u\y = tempy
			u\value = grid(tempx,tempy)
			u\prevundo = Not firstundo
			undocount = undocount + 1
			undobuf(undocount) = u
		ElseIf((undoflag = False) And (undocount = 9999)) Then
			If(redoflag = False) clearredobuffer()
			i = 1
			Delete undobuf(i)
			While((undobuf(i+1)\prevundo = True))
				i = i + 1
				Delete undobuf(i)
			Wend
			For j = (i + 1) To 9999
				undobuf((j-i)) = undobuf(j)
			Next ;j
			undocount = undocount - i
		ElseIf((undoflag = True) And (redoflag = False) And (redocount < 9999)) Then
			u.undo = New undo
			u\x = tempx
			u\y = tempy
			u\value = grid(tempx,tempy)
			u\prevundo = Not firstundo
			redocount = redocount + 1
			redobuf(redocount) = u
		End If
		;debuglog "undo count = " + undocount + " redo count = " + redocount
		firstundo = False
	End If
	;debuglog undocount
	If (gridpiece <> 0)
		scoregrid(tempx,tempy)=0
		Select pieces(gridpiece)\ptype
			Case 1
				Rect xx, yy, 16, 16, 1
				DrawImage blankpiece,xx,yy
				grid(tempx,tempy) = 0
			Case 2
				Rect xx, yy, 32, 32, 1
				DrawImage blankpiece,xx,yy
				DrawImage blankpiece,xx + 16,yy
				DrawImage blankpiece,xx,yy + 16
				DrawImage blankpiece,xx + 16,yy + 16
				grid(tempx,tempy) = 0
				grid(tempx+1,tempy) = 0
				grid(tempx,tempy+1) = 0
				grid(tempx+1,tempy+1) = 0
			Case 3
				Rect xx, yy, 48, 48, 1
				Select pieces(gridpiece)\value
					Case 200
						If(grid(tempx,tempy+1)=0) Then DrawImage blankpiece,xx,yy + 16
						If(grid(tempx+1,tempy+1)=0) Then DrawImage blankpiece,xx + 16,yy + 16
						If(grid(tempx+2,tempy+1)=0) Then DrawImage blankpiece,xx + 32,yy + 16
						If(grid(tempx,tempy+1)>127) Then
							DrawImage blankpiece,xx,yy + 16
							grid(tempx,tempy+1) = 0
						ElseIf(grid(tempx,tempy+1)<>0)
							DrawImage pieces(grid(tempx,tempy+1))\gfx,xx,yy + 16
						End If
						If(grid(tempx+1,tempy+1)>127) Then
							DrawImage blankpiece,xx + 16,yy + 16
							grid(tempx+1,tempy+1) = 0
						ElseIf(grid(tempx+1,tempy+1)<>0)
							DrawImage pieces(grid(tempx+1,tempy+1))\gfx,xx + 16,yy + 16
						End If
						If(grid(tempx+2,tempy+1)>127) Then
							DrawImage blankpiece,xx + 32,yy + 16
							grid(tempx+2,tempy+1) = 0
						ElseIf(grid(tempx+2,tempy+1)<>0)
							DrawImage pieces(grid(tempx+2,tempy+1))\gfx,xx + 32,yy + 16
						End If
					Case 202
						If(grid(tempx,tempy+1)=0) Then DrawImage blankpiece,xx,yy + 16
						If(grid(tempx+1,tempy+1)=0) Then DrawImage blankpiece,xx + 16,yy + 16
						If(grid(tempx+2,tempy+1)=0) Then DrawImage blankpiece,xx + 32,yy + 16
						If(grid(tempx,tempy+1)>127) Then
							DrawImage blankpiece,xx,yy + 16
							grid(tempx,tempy+1) = 0
						ElseIf(grid(tempx,tempy+1)<>0)
							DrawImage pieces(grid(tempx,tempy+1))\gfx,xx,yy + 16
						End If
						If(grid(tempx+1,tempy+1)>127) Then
							DrawImage blankpiece,xx + 16,yy + 16
							grid(tempx+1,tempy+1) = 0
						ElseIf(grid(tempx+1,tempy+1)<>0)
							DrawImage pieces(grid(tempx+1,tempy+1))\gfx,xx + 16,yy + 16
						End If
						If(grid(tempx+2,tempy+1)>127) Then
							DrawImage blankpiece,xx + 32,yy + 16
							grid(tempx+2,tempy+1) = 0
						ElseIf(grid(tempx+2,tempy+1)<>0)
							DrawImage pieces(grid(tempx+2,tempy+1))\gfx,xx + 32,yy + 16
						End If
					Default
						DrawImage blankpiece,xx,yy + 16
						DrawImage blankpiece,xx + 16,yy + 16
						DrawImage blankpiece,xx + 32,yy + 16
						grid(tempx,tempy+1) = 0
						grid(tempx+1,tempy+1) = 0
						grid(tempx+2,tempy+1) = 0
				End Select
				DrawImage blankpiece,xx,yy
				DrawImage blankpiece,xx + 16,yy
				DrawImage blankpiece,xx + 32,yy
				DrawImage blankpiece,xx,yy + 32
				DrawImage blankpiece,xx + 16,yy + 32
				DrawImage blankpiece,xx + 32,yy + 32
				grid(tempx,tempy) = 0
				grid(tempx+1,tempy) = 0
				grid(tempx+2,tempy) = 0
				
				grid(tempx,tempy+2) = 0
				grid(tempx+1,tempy+2) = 0
				grid(tempx+2,tempy+2) = 0
		End Select
	EndIf
			
	If(p<>Null)
		Select p\ptype
			Case 1
			Case 2
				checkgrid(x+1,y,Null,True)
				setprevundo()
				checkgrid(x,y+1,Null,True)
				setprevundo()
				checkgrid(x+1,y+1,Null,True)
				setprevundo()
			Case 3
				Select p\value
					Case 200
						If(grid(x,y+1)>127) Then 
							checkgrid(x,y+1,Null,True)
							setprevundo()
						End If
						If(grid(x+1,y+1)>127) Then 
							checkgrid(x+1,y+1,Null,True)
							setprevundo()
						End If
						If(grid(x+2,y+1)>127) Then 
							checkgrid(x+2,y+1,Null,True)
							setprevundo()
						End If
					Case 202
						If(grid(x,y+1)>127) Then 
							checkgrid(x,y+1,Null,True)
							setprevundo()
						End If
						If(grid(x+1,y+1)>127) Then 
							checkgrid(x+1,y+1,Null,True)
							setprevundo()
						End If
						If(grid(x+2,y+1)>127) Then 
							checkgrid(x+2,y+1,Null,True)
							setprevundo()
						End If
					Default
						checkgrid(x,y+1,Null,True)
						setprevundo()
						checkgrid(x+1,y+1,Null,True)
						setprevundo()
						checkgrid(x+2,y+1,Null,True)
						setprevundo()
				End Select
				checkgrid(x+1,y,Null,True)
				setprevundo()
				checkgrid(x+2,y,Null,True)
				setprevundo()
				checkgrid(x,y+2,Null,True)
				setprevundo()
				checkgrid(x+1,y+2,Null,True)
				setprevundo()
				checkgrid(x+2,y+2,Null,True)
				setprevundo()
		End Select
	EndIf
	
	
End Function



Function setprevundo()
	Return 0
	If(undocount <> 0)
		If(undoflag=False) undobuf(undocount)\prevundo = True
	End If
End Function

Function initgfx()

	For i = 1 To 255 Step 1
		p.piece = New piece
		pieces(i) = p
		;imagestr$ = "images\" + Right$(Hex$(i),2) + ".bmp"
		imagestr$ = "x" + Lower$(Right$(Hex$(i),2))
		;debuglog imagestr$ + " " + i
		;Flip
		;pieces(i)\gfx = LoadImage(imagestr$)
		pieces(i)\gfx = GetPackedImage(imagestr$,trackpieceimage)
		
		pieces(i)\Value = i
		Read pieces(i)\point
		Read pieces(i)\ent
		Read pieces(i)\ext
		If (i < 128) Then
			pieces(i)\ptype = 1
		ElseIf (i < 188) Then
			pieces(i)\ptype = 2
		Else
			pieces(i)\ptype = 3
		EndIf
	Next ;i
	;blankpiece = LoadImage("images\00.bmp")
	blankpiece = GetPackedImage("x00", trackpieceimage)
	resetgrid()
	FreePack trackpieceimage
End Function

Function programcleanup()
	For p.piece = Each piece
		FreeImage p\gfx
		Delete p
	Next ;piece
	clearundobuffer()
	clearredobuffer()
	FreeImage blankpiece
	FreeGadget label1
	FreeGadget pointlabel
	FreeGadget scorelabel
	FreeGadget loopcompletelaber
	FreeGadget loopscorelabel
	FreeGadget mode1but
	FreeGadget mode2but
	FreeGadget mode3but
	FreeGadget mode4but
	FreeGadget passbut
	FreeGadget loadpassbut
	FreeGadget undofunction
	FreeGadget namelabel
	FreeGadget nametext
	FreeGadget planetlabel
	FreeGadget planetcombo
	FreeGadget passtextarea
	FreeGadget recordlabel
	FreeGadget drivernamelabel
	FreeGadget drivercarlabel
	FreeGadget redobut
End Function

Function nesw(p.piece,mode)
	If mode = 1 Then
		Select p\ptype
			Case 1
				Return p\ent
			Case 2
				Select p\ent
					Case 1
						Return 1
					Case 2
						Return 1
					Case 3
						Return 2
					Case 4
						Return 3
					Case 5
						Return 2
					Case 6
						Return 3
					Case 7
						Return 4
					Case 8
						Return 4
				End Select
			Case 3
				Select p\ent
					Case 1
						Return 1
					Case 2
						Return 1
					Case 3
						Return 1
					Case 4
						Return 2
					Case 5
						Return 3
					Case 6
						Return 2
					Case 7
						Return 3
					Case 8
						Return 2
					Case 9
						Return 3
					Case 10
						Return 4
					Case 11
						Return 4
					Case 12
						Return 4
				End Select
		End Select
	Else
		Select p\ptype
			Case 1
				Return p\ext
			Case 2
				Select p\ext
					Case 1
						Return 1
					Case 2
						Return 1
					Case 3
						Return 2
					Case 4
						Return 3
					Case 5
						Return 2
					Case 6
						Return 3
					Case 7
						Return 4
					Case 8
						Return 4
				End Select
			Case 3
				Select p\ext
					Case 1
						Return 1
					Case 2
						Return 1
					Case 3
						Return 1
					Case 4
						Return 2
					Case 5
						Return 3
					Case 6
						Return 2
					Case 7
						Return 3
					Case 8
						Return 2
					Case 9
						Return 3
					Case 10
						Return 4
					Case 11
						Return 4
					Case 12
						Return 4
				End Select
		End Select
	EndIf		
End Function

Function xyoffset(x,y,ptype,ent)
	Select ptype
		Case 1
			Select ent
				Case 1
					Return (x) + ((y-1) Shl 8)
				Case 2
					Return (x-1) + ((y) Shl 8)
				Case 3
					Return (x+1) + ((y) Shl 8)
				Case 4
					Return (x) + ((y+1) Shl 8)
			End Select
		Case 2
			Select ent
				Case 1
					Return (x) + ((y-1) Shl 8)
				Case 2
					Return (x+1) + ((y-1) Shl 8)
				Case 3
					Return (x-1) + ((y) Shl 8)
				Case 4
					Return (x+2) + ((y) Shl 8)
				Case 5
					Return (x-1) + ((y+1) Shl 8)
				Case 6
					Return (x+2) + ((y+1) Shl 8)
				Case 7
					Return (x) + ((y+2) Shl 8)
				Case 8
					Return (x+1) + ((y+2) Shl 8)
			End Select
		Case 3
			Select ent
				Case 1
					Return (x) + ((y-1) Shl 8)
				Case 2
					Return (x+1) + ((y-1) Shl 8)
				Case 3
					Return (x+2) + ((y-1) Shl 8)
				Case 4
					Return (x-1) + ((y) Shl 8)
				Case 5
					Return (x+3) + ((y) Shl 8)
				Case 6
					Return (x-1) + ((y+1) Shl 8)
				Case 7
					Return (x+3) + ((y+1) Shl 8)
				Case 8
					Return (x-1) + ((y+2) Shl 8)
				Case 9
					Return (x+3) + ((y+2) Shl 8)
				Case 10
					Return (x) + ((y+3) Shl 8)
				Case 11
					Return (x+1) + ((y+3) Shl 8)
				Case 12
					Return (x+2) + ((y+3) Shl 8)
			End Select
	End Select
End Function

Function isconnectionpossible(dir1, dir2)
	Select dir1
		Case 1
			If(dir2=4) Then
				Return True
			Else
				Return False
			EndIf
		Case 2
			If(dir2=3) Then
				Return True
			Else
				Return False
			EndIf
		Case 3
			If(dir2=2) Then
				Return True
			Else
				Return False
			EndIf
		Case 4
			If(dir2=1) Then
				Return True
			Else
				Return False
			EndIf
	End Select
End Function

Function ispiececonnected(p1.piece, x1, y1, p2.piece, x2, y2)
	piececonnect = False
	piece1con1 = 0
	piece1con2 = 0
	piece2con1 = 0
	piece2con2 = 0
	If(p1=Null Or p2=Null)
		Return False
	EndIf
	piece1con1 = nesw(p1,1)
	piece1con2 = nesw(p1,2)
	piece2con1 = nesw(p2,1)
	piece2con2 = nesw(p2,2)
	piece1x = 0
	piece1y = 0
	piece2x = 0
	piece2y = 0
	
	If(isconnectionpossible(piece1con1,piece2con1) And piececonnect = False) Then
		tmp = xyoffset(x1,y1,p1\ptype,p1\ent)
		piece1x = ((tmp Shl 24) Shr 24)
		piece1y = ((tmp Shr 8))
		tmp = xyoffset(x2,y2,p2\ptype,p2\ent)
		piece2x = ((tmp Shl 24) Shr 24)
		piece2y = ((tmp Shr 8))
		If (piece1x = piece2x And ((piece1y = (piece2y + 1)) Or ((piece1y + 1) = piece2y))) Then
			piececonnect = True
		EndIf
		If (piece1y = piece2y And ((piece1x = (piece2x + 1)) Or ((piece1x + 1) = piece2x))) Then
			piececonnect = True
		EndIf		
	EndIf
	
	If(isconnectionpossible(piece1con2,piece2con1) And piececonnect = False) Then
		tmp = xyoffset(x1,y1,p1\ptype,p1\ext)
		piece1x = ((tmp Shl 24) Shr 24)
		piece1y = ((tmp Shr 8))
		tmp = xyoffset(x2,y2,p2\ptype,p2\ent)
		piece2x = ((tmp Shl 24) Shr 24)
		piece2y = ((tmp Shr 8))
		If (piece1x = piece2x And ((piece1y = (piece2y + 1)) Or ((piece1y + 1) = piece2y))) Then
			piececonnect = True
		EndIf
		If (piece1y = piece2y And ((piece1x = (piece2x + 1)) Or ((piece1x + 1) = piece2x))) Then
			piececonnect = True
		EndIf
	EndIf
	
	If(isconnectionpossible(piece1con1,piece2con2) And piececonnect = False) Then
		tmp = xyoffset(x1,y1,p1\ptype,p1\ent)
		piece1x = ((tmp Shl 24) Shr 24)
		piece1y = ((tmp Shr 8))
		tmp = xyoffset(x2,y2,p2\ptype,p2\ext)
		piece2x = ((tmp Shl 24) Shr 24)
		piece2y = ((tmp Shr 8))
		If (piece1x = piece2x And ((piece1y = (piece2y + 1)) Or ((piece1y + 1) = piece2y))) Then
			piececonnect = True
		EndIf
		If (piece1y = piece2y And ((piece1x = (piece2x + 1)) Or ((piece1x + 1) = piece2x))) Then
			piececonnect = True
		EndIf
	EndIf
	
	If(isconnectionpossible(piece1con2,piece2con2) And piececonnect = False) Then
		tmp = xyoffset(x1,y1,p1\ptype,p1\ext)
		piece1x = ((tmp Shl 24) Shr 24)
		piece1y = ((tmp Shr 8))
		tmp = xyoffset(x2,y2,p2\ptype,p2\ext)
		piece2x = ((tmp Shl 24) Shr 24)
		piece2y = ((tmp Shr 8))
		If (piece1x = piece2x And ((piece1y = (piece2y + 1)) Or ((piece1y + 1) = piece2y))) Then
			piececonnect = True
		EndIf
		If (piece1y = piece2y And ((piece1x = (piece2x + 1)) Or ((piece1x + 1) = piece2x))) Then
			piececonnect = True
		EndIf
	EndIf
	
	Return piececonnect
	
End Function

Function getnextpiece(p.piece,x,y,mode)
	If (mode = 1)
		tmp = xyoffset(x,y,p\ptype,p\ent)
		piecex = ((tmp Shl 24) Shr 24)
		piecey = tmp Shr 8
	Else
		tmp = xyoffset(x,y,p\ptype,p\ext)
		piecex = ((tmp Shl 24) Shr 24)
		piecey = tmp Shr 8
	EndIf
	If (piecex > 0 And piecey > 0) Then
		If((grid(piecex,piecey) Shr 8) = 255) Then
			piecex = piecex - 1
			If((grid(piecex,piecey) Shr 8) = 255) Then
				piecex = piecex - 1
				piecey = piecey - (grid(piecex,piecey) Shr 8)
			Else
				piecey = piecey - (grid(piecex,piecey) Shr 8)
			EndIf
		Else
			piecey = piecey - (grid(piecex,piecey) Shr 8)
		EndIf
		Return ((grid(piecex,piecey) Shl 24) Shr 24)  + (piecex Shl 8) + (piecey Shl 16)
	EndIf
	Return 0
End Function

Function checkloop()
	startpiecex = 0
	startpiecey = 0
	startpiececount = 0
	
	If (checkloopflag = False)
		For x = 1 To 16
			For y = 1 To 16
				loopgrid(x,y) = scoregrid(x,y)
			Next ;y
		Next ;x
		Return True
	End If

	For x = 1 To 16
		For y = 1 To 16
			loopgrid(x,y) = 0
			If (grid(x,y)=1) Then
				;debuglog "start piece found"
				startpiecex = x
				startpiecey = y
				startpiececount = startpiececount + 1
			EndIf
		Next ;y
	Next ;x
	prevx = 0
	prevy = 0
	prevp = 0
	currx = startpiecex
	curry = startpiecey
	currp = 1
	If (startpiecex <> 0 And startpiecey <> 0 And startpiececount = 1) Then
		Repeat
			tmp = getnextpiece(pieces(currp),currx,curry,1)
			loopgrid(currx,curry) = 1
			nextx = ((tmp Shl 16) Shr 24)
			nexty = (tmp Shr 16)
			nextp = ((tmp Shl 24) Shr 24)
			If ((nextx = prevx) And (nexty = prevy) And (nextp = prevp)) Then
				tmp = getnextpiece(pieces(currp),currx,curry,2)
				nextx = ((tmp Shl 16) Shr 24)
				nexty = (tmp Shr 16)
				nextp = ((tmp Shl 24) Shr 24)
			EndIf
			If(nextp <> 0) Then 
				piececonnect = ispiececonnected(pieces(currp),currx,curry,pieces(nextp),nextx,nexty)
			Else
				piececonnect = False
			EndIf
			prevx = currx
			prevy = curry
			prevp = currp
			currx = nextx
			curry = nexty
			currp = nextp
		Until ((currp = 1) Or (piececonnect = False))
		Return piececonnect
	ElseIf (startpiececount > 1)
		;debuglog "Only one Start piece is allowed, please delete unused start pieces"
		Return -2
	ElseIf (startpiececount = 0)
		;debuglog "Your track needs a start piece, please add one."
		Return -1
	EndIf

End Function

Function checkloopscore()
	score = 0
	If(checkloopscoreflag = False) Then Return 1
	For x = 1 To 16
		For y = 1 To 16
			If(loopgrid(x,y) = 1) Then
				score = score + pieces(grid(x,y))\point
			EndIf
		Next ;y
	Next ;x
	Return score
End Function

Function checktotalscore()
	score = 0
	For x = 1 To 16
		For y = 1 To 16
			If(scoregrid(x,y) = 1) Then
				score = score + pieces(grid(x,y))\point
			EndIf
		Next ;y
	Next ;x
	Return score
End Function

Function loadpassword(password$,showerror=True,changerecord=False)
	DebugLog "changerecord = " + changerecord
	passstr$ = Upper$(password$)
	changerecordstr$ = ""
	passstring$="0123456789-+#?C$FHJKLMNPQ!TVWXY="
	commonerr1$="O"
	commonerr2$="0"
	passdata=CreateBank(500)
	rlepassdata=CreateBank(500)
	passchar=CreateBank(500)
	For i = 0 To 499
		PokeByte passdata, i, 0
		PokeByte rlepassdata, i, 0
	Next ;i
	passstrlen = Len(passstr$)
	passlen = 0
	For i = 1 To passstrlen
		passchr=Instr(passstring$,Mid$(passstr$,i,1))
		;debuglog "Passchar = '" + Mid$(passstr$,i,1) + "' Instr = " + passchr
		If (passchr<>0) Then
			PokeByte passchar, passlen, passchr-1
			changerecordstr$ = changerecordstr$ + Mid$(passstr$,i,1)
			passlen = passlen + 1
		Else
			passchr=Instr(commonerr1$,Mid$(passstr$,i,1))
			If(passchr<>0)
				changerecordstr$ = changerecordstr$ + Mid$(commonerr2$,passchr,1)
				passchr=Instr(passstring$,Mid$(commonerr2$,passchr,1))
				PokeByte passchar, passlen, passchr-1
				passlen = passlen + 1
			End If
		EndIf
	Next ;i
	
	For i = 0 To (passlen - 1)
		passchr=PeekByte(passchar,i)
		For j = 0 To 4
			tmpdata = ((((passchr Shr (4 - j)) Shl 31) Shr 31) Shl (7 - (((i*5)+j)Mod 8)))
			PokeByte rlepassdata, (((i*5)+j)/8), PeekByte(rlepassdata,(((i*5)+j)/8)) + tmpdata
			;passchr = passchr + (((PeekByte(rlepassdata,(((i*5)+j)/8)) Shr (7 - (((i*5)+j) Mod 8))) Shl 31) Shr (31 - (4 - j)))
		Next ;j
	Next ;i
	
	datalen = PeekShort(rlepassdata, 0)
	If ((datalen = 0) Or (datalen > 499)) Then
		If(showerror) Notify "Password Error"
		Return -1
	End If
	dataoffset = 2
	i = 0
	While ( i < datalen )
		tmpdata=PeekByte(rlepassdata,dataoffset)
		dataoffset = dataoffset + 1
		If(tmpdata <> 0)
			PokeByte passdata, i, tmpdata
			i = i + 1
		Else
		    tmpdata=PeekByte(rlepassdata,dataoffset)
		    dataoffset = dataoffset + 1
			PokeByte passdata, i, 0
			i = i + 1
		    While (tmpdata > 0)
				tmpdata = tmpdata - 1
				i = i + 1
				PokeByte passdata, i, 0
			Wend
		End If
	Wend
	fixedbyte = PeekByte(passdata, 1)
	If ((fixedbyte <> $A5) And (MenuChecked(fixedoverride) = False)) Then
		If (showerror) Notify "Password Error - Fixed Byte = $" + Right$(Hex$(fixedbyte),2) + " Expected $A5"
		Return -1
	End If
	checksum = PeekByte(passdata, 0)
	tmpdata = 0
	For i = 1 To (datalen - 1)
		tmpdata = tmpdata + PeekByte(passdata,i)
	Next ;i
	tmpdata = ((tmpdata Shl 24) Shr 24)
	If(((checksum = tmpdata) Or (MenuChecked(checkoverride))) And (changerecord = False)) Then
	;	Print "Decoding Successful"
		resetgrid()
		namestr$ = ""
		For i = 0 To 13
			namestr$ = namestr$ + Chr$(PeekByte(passdata, 12+i)+32)
		Next ;i
		SetGadgetText nametext, namestr$
		SelectGadgetItem planetcombo, PeekByte(passdata, 3)
		datapointer = 0
		
		For  i = 0 To 31
			databyte = PeekByte(passdata, 28+i)
			For j = 0 To 7
				x = (((i*8)+j) Mod 16) + 1
				y = (((i*8)+j) / 16) + 1
				tmpdata = (((databyte Shr (7 - j)) Shl 31) Shr 31)
				If(tmpdata = 1) Then
					drawpiece(x,y,pieces(PeekByte(passdata,60+datapointer)))
					datapointer = datapointer + 1
				End If
			Next ;j
		Next ;i
		refreshscore()
		vehicle = PeekByte(passdata,2)
		recordtimeS = PeekByte(passdata,27)
		recordtimeMS = PeekByte(passdata,26)
		driver$ = ""
		For i = 0 To 7
			driver$ = driver$ + Chr$(PeekByte(passdata,4+i)+32)
		Next ;i
	ElseIf (changerecord = True)
		tmpnamestr$ = Upper$(Left$(TextFieldText(nametext),14)) + "              "
		tmpplanet = SelectedGadgetItem(planetcombo)
		tmpvehicle = vehicle
		tmpRecordTimeS = recordtimeS
		tmpRecordTimeMS = recordtimeMS
		tmpDriver$ = driver$
		changerecordlen = Len(changerecordstr$)
		namestr$ = ""
		For i = 0 To 13
			namestr$ = namestr$ + Chr$(PeekByte(passdata, 12+i)+32)
		Next ;i
		SetGadgetText nametext,namestr$
		SelectGadgetItem planetcombo, PeekByte(passdata, 3)
		vehicle = PeekByte(passdata,2)
		recordtimeS = PeekByte(passdata,27)
		recordtimeMS = PeekByte(passdata,26)
		driver$ = ""
		For i = 0 To 7
			driver$ = driver$ + Chr$(PeekByte(passdata,4+i)+32)
		Next ;i
		
		tmppassstr$ = createpassword$()
		DebugLog changerecordstr$
		DebugLog tmppassstr$
		
		passwordvalid = True
		For i = 1 To changerecordlen
			If(Mid$(changerecordstr$,i,1) <> Mid$(tmppassstr$,i,1)) Then
				passwordvalid = False
			End If
		Next ;i
		If (passwordvalid = False) Then
			If(showerror) Then Notify "Password for Changing set record incorrect."
			SetGadgetText nametext,tmpnamestr$
			SelectGadgetItem planetcombo, tmpplanet
			vehicle = tmpvehicle
			RecordTimeS = tmpRecordTimeS
			RecordTimeMS = tmpRecordTimeMS
			driver$ = tmpDriver$
		End If
		
	Else
		If(showerror) Notify "Password Error - Calculated Checksum = $" + Right$(Hex$(tmpdata),2) + " Expected Checksum = $" + Right$(Hex$(checksum),2)
		Return -1
	;	DebugLog "Checksum = " + checksum + " Calculated Checksum = " + tmpdata
	End If
	
	FreeBank (passdata)
	FreeBank (rlepassdata)
	FreeBank (passchar)

	clearundobuffer()
	clearredobuffer()
	
	Return 0
			
End Function



Function createpassword$()
	passstring$="0123456789-+#?C$FHJKLMNPQ!TVWXY="
	passdata=CreateBank(500)
	rlepassdata=CreateBank(500)
	For i = 0 To 499
		PokeByte passdata, i, 0
		PokeByte rlepassdata, i, 0
	Next ;i
	If(checkloop()=1) Then
		If(checkloopscore()<256) Then
			planet=SelectedGadgetItem(planetcombo)
			name$=Upper$(Left$(TextFieldText(nametext),14)) + "              "
			validnamechar$ = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!?,.-'&_@ "
			If(checkloopflag=True) Then
				If(Left$(name$,14)="              ") Then
					createpassword_err = 2 
					Return ""
				End If
				For i = 1 To 14
					If(Instr(validnamechar$,Mid$(name$,i,1))=0) Then 
						createpassword_err = 3
						Return ""
					End If
				Next ;i
			End If
			If(editflag=True) Then
				racercar = Rnd(1,3)
				Select racercar
					Case 1
						racername$="C.FALCON"
						racercar=$00
					Case 2
						racername$="STEWART "
						racercar=$01
					Case 3
						racername$="R.SUZAKU"
						racercar=$1E
				End Select
				vehicle=racercar
				driver$=racername$
				recordtimeMS = 225
				recordtimeS  = 157
				PokeByte passdata,26,225
				PokeByte passdata,27,157
				editflag=False
			Else
				racername$=driver$
				racercar=vehicle
				PokeByte passdata,26,recordtimeMS
				PokeByte passdata,27,recordtimeS
			End If
			PokeByte passdata,1,165
			PokeByte passdata,2,racercar
			PokeByte passdata,3,planet
			For i = 1 To 8
				PokeByte passdata,4+(i-1),Asc(Mid$(racername$,i,1))-32
			Next ;i
			For i = 1 To 14
				PokeByte passdata,12+(i-1),Asc(Mid$(name$,i,1))-32
			Next ;i
			
			
			For i = 1 To 256 Step 8
				databyte = 0
				x = ((i-1) Mod 16) + 1
				y = ((i-1) / 16) + 1
				bitnum = (i - 1) Mod 8
				bytenum = (i - 1) / 8
				;PokeByte passdata, 28 + bytenum, PeekByte(passdata,28 + bytenum) + (loopgrid(x,y) Shl (7 - bitnum))
				For j = 0 To 7
					If (loopgrid(x+j,y)=1) Then
						databyte = databyte + (1 Shl (7 - j))
					End If
				Next ;j
				PokeByte passdata, 28 + bytenum, databyte
			Next ;i
			datacount = 0
			For i = 1 To 256
				x = ((i-1) Mod 16) + 1
				y = ((i-1) / 16) + 1
				If(loopgrid(x,y)=1) Then
					PokeByte passdata, 60+datacount, grid(x,y)
					datacount = datacount + 1
				End If
			Next ;i
			datalen = 60 + datacount
			datasum = 0
			For i=1 To datalen
				datasum = datasum + PeekByte(passdata,i)
			Next ;i
			PokeByte passdata, 0, datasum
			
			;PokeShort rlepassdata, 0, datalen
			PokeByte rlepassdata, 0, ((datalen Shl 24) Shr 24)
			PokeByte rlepassdata, 1, (((datalen Shr 8) Shl 24) Shr 24)
			rlepasslen = 0
			dataoffset = 0
			i = 0
			;For i = 0 To datalen Step 4
;;;;;;;;;			;	Print "passdata " + i + " = " + Hex$(PeekInt(passdata,i))
			;Next ;i
			i = 0
			While i <= datalen
				
				If(PeekByte(passdata,i)<>0)
					PokeByte rlepassdata, dataoffset+2, PeekByte(passdata,i)
					rlepasslen = rlepasslen + 1
					i = i + 1
					dataoffset = dataoffset + 1
				Else
					zerocount = 1
					While (PeekByte(passdata,i+zerocount)=0 And (i+zerocount) <= datalen)
						zerocount = zerocount + 1
					Wend
					PokeByte rlepassdata, dataoffset+2, 0
					PokeByte rlepassdata, dataoffset+2+1,zerocount - 1
					dataoffset = dataoffset + 2
					rlepasslen = rlepasslen + 2
					i = i + zerocount
				End If
			Wend
			
			;For i = 0 To rlepasslen Step 4
				;debuglog Hex$(PeekInt(rlepassdata,i))
			;Next ;i
			
			DebugLog "rlepasslen = " + rlepasslen
			passlen = (rlepasslen * 8)
			DebugLog "passlen = " + passlen + " rlepasslen = " + passlen / 5
			rlepasslen = passlen / 5
			passlen = passlen Mod 5
			DebugLog "passlen = " + passlen
			If (passlen>0) Then rlepasslen = rlepasslen + 1
			;rlepasslen = rlepasslen Mod 5
			;If (rlepasslen > 0) Then passlen = passlen + 1
			
			passstr$ = ""
			;debuglog rlepasslen
			
			For i = 0 To rlepasslen - 1
				passchr = 0
				
				For j = 0 To 4
					passchr = passchr + (((PeekByte(rlepassdata,(((i*5)+j)/8)) Shr (7 - (((i*5)+j) Mod 8))) Shl 31) Shr (31 - (4 - j)))
				Next ;j
				;debuglog "passchr = " + passchr
				passstr$ = passstr$ + Mid$(passstring$,passchr+1,1)
				;If (((i + 1) Mod 4 = 0) And (i <> 0)) Then
				;	passstr$ = passstr$ + " "
				;EndIf
				;If (((i + 1) Mod 20 = 0) And (i <> 0)) Then
					;debuglog passstr$
					
				;	passstr$ = passstr$ + Chr$(13)
					;passstr$ = ""
				;EndIf
			Next ;i
			;debuglog passstr$
			;AddTextAreaText passtextarea, passstr$
			
		;Else
		;	SetGadgetText passtextarea, "Your track score is too big, please reduce the number of pieces in your course."
		End If
	;Else
		;SetGadgetText passtextarea, "Check the status above. The password cannot be created until you have a complete loop with only one start piece."
	End If
	FreeBank(passdata)
	FreeBank(rlepassdata)
	DebugLog "passlen (before) = " + Len(passstr$)
	;While(Right$(passstr$,1)="0")
	;	passstr$=Mid$(passstr$,1,Len(passstr$)-1)
	;Wend
	DebugLog "passlen (after) = " + Len(passstr$)
	If(Len(passstr$)>0) Then
		createpassword_err = 0
		Return passstr$
	Else
		createpassword_err = -1
		Return ""
	End If
End Function

Function checkversion()
	;tcp=OpenTCPStream ( "caitsith2.com", 80 )
	;If (Not tcp) Then Return -1
		
	;WriteLine tcp, "GET /climax/update.txt HTTP/1.0"
	;WriteLine tcp, "Host: caitsith2.com"
	;WriteLine tcp, "User-Agent: F-Zero Climax Track Editor version " + verstr$
	;WriteLine tcp,Chr$(10)
	
	;If Eof(tcp) Then Return -1
	
	;versionflag = False
	;While Not Eof(tcp)
	;	version$ = ReadLine$ ( tcp )
	;	If (version$ = verstr$) Then versionflag = True
	;Wend
	;
	;If (versionflag = True) Return 1
	;
	;If Eof(tcp)=1 Then Return 0 Else Return -1
	;
	;CloseTCPStream tcp
	Return 1
End Function

Function scratcharea()
	;DumpTiles()
	;Return 0
	;Used For testing code For this project. :)
End Function


;1x1 point values / entrance offsets / exit offsets
.Data1x1
Data 2, 1, 4 ;01
Data 2, 1, 4
Data 2, 2, 3
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 1, 4 ;10
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4 ;1B
Data 2, 2, 3 ;1C
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3 ;27
Data 2, 1, 4 ;28
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4 ;2D
Data 2, 2, 3 ;2E
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3 ;33
Data 2, 1, 4 ;34
Data 2, 1, 4
Data 2, 1, 4 ;36
Data 2, 2, 3 ;37
Data 2, 2, 3
Data 2, 2, 3 ;39
Data 2, 1, 4 ;3A
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4 ;3F
Data 2, 2, 3 ;40
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3 ;45
Data 2, 1, 4 ;46
Data 2, 1, 4
Data 2, 1, 4 ;48
Data 2, 2, 3 ;49
Data 2, 2, 3
Data 2, 2, 3 ;4B
Data 2, 1, 4 ;4C
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4
Data 2, 1, 4 ;51
Data 2, 2, 3 ;52
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3
Data 2, 2, 3 ;57
Data 2, 1, 4 ;58
Data 2, 1, 4
Data 2, 1, 4 ;5A
Data 2, 2, 3 ;5B
Data 2, 2, 3
Data 2, 2, 3 ;5D
Data 2, 1, 4 ;5E
Data 2, 1, 4 ;5F
Data 2, 2, 3 ;60
Data 2, 2, 3 ;61
Data 6, 1, 4
Data 6, 1, 4
Data 6, 2, 3
Data 6, 2, 3
Data 6, 1, 4
Data 6, 2, 3
Data 2, 1, 4
Data 2, 2, 3
Data 2, 1, 4
Data 2, 2, 3
Data 2, 1, 4
Data 2, 1, 4
Data 2, 2, 3
Data 2, 2, 3
Data 3, 1, 3
Data 3, 2, 1
Data 3, 4, 3
Data 3, 2, 4
Data 3, 1, 3
Data 3, 2, 1
Data 3, 4, 3
Data 3, 2, 4
Data 3, 1, 3
Data 3, 2, 1
Data 3, 4, 3
Data 3, 2, 4
Data 3, 1, 3
Data 3, 2, 1
Data 3, 4, 3
Data 3, 2, 4

;2x2 point values / entrance offsets / exit offsets
Data 7, 1,6
Data 7, 5,2
Data 7, 7,4
Data 7, 3,8
Data 10,1,6
Data 10,5,2
Data 10,7,4
Data 10,3,8
Data 10,1,6
Data 10,5,2
Data 10,7,4
Data 10,3,8
Data 10,1,6
Data 10,5,2
Data 10,7,4
Data 10,3,8
Data 7, 1,6
Data 7, 5,2
Data 7, 7,4
Data 7, 3,8
Data 10,1,6
Data 10,5,2
Data 10,7,4
Data 10,3,8
Data 10,1,6
Data 10,5,2
Data 10,7,4
Data 10,3,8
Data 7, 1,6
Data 7, 5,2
Data 7, 7,4
Data 7, 3,8
Data 7, 1,6
Data 7, 5,2
Data 7, 7,4
Data 7, 3,8
Data 10,1,6
Data 10,5,2
Data 10,7,4
Data 10,3,8
Data 10,1,6
Data 10,5,2
Data 10,7,4
Data 10,3,8
Data 10,1,6
Data 10,5,2
Data 10,7,4
Data 10,3,8
Data 9, 1,8
Data 9, 2,7
Data 9, 5,4
Data 9, 3,6
Data 9, 1,8
Data 9, 2,7
Data 9, 3,6
Data 9, 5,4
Data 8, 1,6
Data 8, 5,2
Data 8, 7,4
Data 8, 3,8

;3x3 point values / entrance offsets / exit offsets
Data 10, 1,9 
Data 10, 8,3 
Data 10,10,5 
Data 10, 4,12
Data 10, 1,9
Data 10, 8,3
Data 10,10,5
Data 10, 4,12
Data 10, 1,9
Data 10, 8,3
Data 10,10,5
Data 10, 4,12
Data 4, 2,11
Data 4, 6,7
Data 4, 2,11
Data 4, 6,7
Data 20,1,3
Data 20,10,12
Data 20,5,9
Data 20,4,8
Data 20,1,3
Data 20,10,12
Data 20,5,9
Data 20,4,8
Data 10,3,10
Data 10,1,12
Data 10,8,5
Data 10,4,9
Data 10,3,10
Data 10,1,12
Data 10,8,5
Data 10,4,9
Data 10,3,10
Data 10,1,12
Data 10,8,5
Data 10,4,9
Data 12,3,10
Data 12,1,12
Data 12,8,5
Data 12,4,9
Data 12,3,10
Data 12,1,12
Data 12,8,5
Data 12,4,9
Data 10,1,9
Data 10,3,8
Data 10,10,5
Data 10,4,12
Data 16,2,3
Data 16,10,11
Data 16,7,9
Data 16,4,6
Data 16,1,2
Data 16,11,12
Data 16,5,7
Data 16,6,8
Data 28,2,11
Data 28,2,11
Data 24,6,7
Data 24,6,7
Data 28,2,11
Data 28,2,11
Data 24,6,7
Data 24,6,7
Data 28,2,11
Data 28,2,11
Data 24,6,7
Data 24,6,7