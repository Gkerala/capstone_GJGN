# backend/records/views.py
from django.http import HttpResponse

def index(request):
    return HttpResponse("Records app is working correctly!")
